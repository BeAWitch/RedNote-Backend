package org.rednote.ai.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.entity.EsAiUserNoteVector;
import org.rednote.ai.feign.NoteServiceFeign;
import org.rednote.ai.repository.EsAiUserNoteVectorRepository;
import org.rednote.note.api.entity.WebNote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteVectorSyncService {

    private static final int PAGE_SIZE = 200;
    private static final int EMBED_BATCH_SIZE = 64;

    private final NoteServiceFeign noteServiceFeign;
    private final EsAiUserNoteVectorRepository noteVectorRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${spring.ai.zhipuai.api-key:}")
    private String zhipuApiKey;

    public SyncResult syncAllPublishedNotes() {
        long start = System.currentTimeMillis();
        int syncedNotes = 0;
        int syncedChunks = 0;
        int skippedNotes = 0;
        long currentPage = 1;

        ensureIndex();

        while (true) {
            Page<WebNote> page;
            try {
                page = noteServiceFeign.selectNotePage(currentPage, (long) PAGE_SIZE);
            } catch (Exception e) {
                log.warn("分页查询笔记失败，page={}", currentPage, e);
                break;
            }
            if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
                break;
            }

            List<WebNote> notes = page.getRecords();
            // 仅处理审核通过的笔记
            List<WebNote> toEmbed = new ArrayList<>();
            for (WebNote note : notes) {
                if (note == null || note.getId() == null) {
                    continue;
                }
                if (note.getAuditStatus() == null || note.getAuditStatus() != 1) {
                    skippedNotes++;
                    continue;
                }
                String text = buildEmbeddingText(note);
                if (StrUtil.isBlank(text)) {
                    skippedNotes++;
                    continue;
                }
                toEmbed.add(note);
            }

            if (!toEmbed.isEmpty()) {
                // 分批 embedding
                int embedIndex = 0;
                while (embedIndex < toEmbed.size()) {
                    int end = Math.min(embedIndex + EMBED_BATCH_SIZE, toEmbed.size());
                    List<WebNote> batch = toEmbed.subList(embedIndex, end);
                    List<float[]> embeddings;
                    try {
                        embeddings = embedBatch(batch);
                    } catch (Exception e) {
                        log.warn("embedding 失败，跳过本批 {} 条笔记", batch.size(), e);
                        embedIndex = end;
                        continue;
                    }

                    List<EsAiUserNoteVector> docs = new ArrayList<>(batch.size());
                    for (int j = 0; j < batch.size(); j++) {
                        WebNote note = batch.get(j);
                        EsAiUserNoteVector doc = new EsAiUserNoteVector();
                        doc.setId("n_" + note.getId());
                        doc.setNoteId(note.getId());
                        doc.setUid(note.getUid());
                        doc.setTitle(StrUtil.blankToDefault(note.getTitle(), ""));
                        doc.setContent(StrUtil.blankToDefault(note.getContent(), ""));
                        doc.setEmbedding(embeddings.get(j));
                        doc.setUpdatedAt(Instant.now().toEpochMilli());
                        docs.add(doc);
                        syncedNotes++;
                    }
                    noteVectorRepository.saveAll(docs);
                    syncedChunks += docs.size();
                    embedIndex = end;
                }
            }

            long total = page.getTotal();
            long pages = page.getPages();
            log.info("笔记向量同步进度: 第 {}/{} 页, 已同步 {} 条, 跳过 {} 条, 总笔记数 {}",
                    currentPage, pages, syncedNotes, skippedNotes, total);

            if (currentPage >= pages) {
                break;
            }
            currentPage++;
        }

        long elapsed = System.currentTimeMillis() - start;
        SyncResult result = new SyncResult(syncedNotes, syncedChunks, skippedNotes, elapsed);
        log.info("笔记向量全量同步完成: {}", result);
        return result;
    }

    private void ensureIndex() {
        try {
            var ops = elasticsearchOperations.indexOps(EsAiUserNoteVector.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping();
            }
        } catch (Exception e) {
            log.warn("初始化 ai_user_note_vector 索引失败", e);
        }
    }

    private String buildEmbeddingText(WebNote note) {
        String title = StrUtil.blankToDefault(note.getTitle(), "");
        String content = StrUtil.blankToDefault(note.getContent(), "");
        if (StrUtil.isBlank(title) && StrUtil.isBlank(content)) {
            return "";
        }
        if (StrUtil.isBlank(title)) {
            return content;
        }
        if (StrUtil.isBlank(content)) {
            return title;
        }
        return title + "\n\n" + content;
    }

    private List<float[]> embedBatch(List<WebNote> notes) {
        if (StrUtil.isBlank(zhipuApiKey)) {
            throw new IllegalStateException("未配置 spring.ai.zhipuai.api-key");
        }
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(zhipuApiKey).build();

        List<String> texts = notes.stream()
                .map(this::buildEmbeddingText)
                .toList();

        EmbeddingCreateParams req = EmbeddingCreateParams.builder()
                .model("embedding-3")
                .input(texts)
                .dimensions(2048)
                .build();
        EmbeddingResponse resp = client.embeddings().createEmbeddings(req);
        if (resp == null || resp.getData() == null || resp.getData().getData() == null) {
            throw new IllegalStateException("embedding 调用返回为空");
        }

        List<float[]> vectors = new ArrayList<>();
        resp.getData().getData().forEach(d -> {
            List<Double> emb = d.getEmbedding();
            float[] vec = new float[emb.size()];
            for (int k = 0; k < emb.size(); k++) {
                vec[k] = emb.get(k).floatValue();
            }
            vectors.add(vec);
        });
        return vectors;
    }

    public record SyncResult(int syncedNotes, int syncedChunks, int skippedNotes, long elapsedMs) {
    }
}
