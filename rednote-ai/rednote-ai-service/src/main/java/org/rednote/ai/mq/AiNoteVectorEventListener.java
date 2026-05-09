package org.rednote.ai.mq;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.entity.EsAiUserNoteVector;
import org.rednote.ai.feign.NoteServiceFeign;
import org.rednote.ai.repository.EsAiUserNoteVectorRepository;
import org.rednote.common.constant.MQConstants;
import org.rednote.note.api.entity.WebNote;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 监听 note-service 的 MQ 事件，同步用户笔记向量索引。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiNoteVectorEventListener {

    private final NoteServiceFeign noteServiceFeign;
    private final EsAiUserNoteVectorRepository noteVectorRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${spring.ai.zhipuai.api-key:}")
    private String zhipuApiKey;

    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(value = MQConstants.NOTE_EVENT_EXCHANGE),
                    value = @Queue(value = "note.ai.vector.sync.queue", durable = "true"),
                    key = { MQConstants.NOTE_CREATE_KEY, MQConstants.NOTE_UPDATE_KEY }
            )
    )
    public void handleNoteCreateOrUpdate(Long noteId) {
        if (noteId == null || noteId <= 0) {
            return;
        }

        ensureIndex();

        WebNote note;
        try {
            note = noteServiceFeign.getNoteById(noteId);
        } catch (Exception e) {
            log.warn("拉取笔记失败，noteId={}", noteId, e);
            return;
        }
        if (note == null) {
            return;
        }

        // Only index audited notes to align with search-service behavior.
        if (note.getAuditStatus() == null || note.getAuditStatus() != 1) {
            try {
                noteVectorRepository.deleteByNoteId(noteId);
            } catch (Exception e) {
                log.warn("删除未审核笔记向量失败，noteId={}", noteId, e);
            }
            return;
        }

        String textForEmbedding = buildEmbeddingText(note);
        if (StrUtil.isBlank(textForEmbedding)) {
            return;
        }

        float[] vec;
        try {
            vec = embedOne(textForEmbedding);
        } catch (Exception e) {
            log.warn("笔记 embedding 失败，noteId={}", noteId, e);
            return;
        }

        EsAiUserNoteVector doc = new EsAiUserNoteVector();
        doc.setId("n_" + noteId);
        doc.setNoteId(noteId);
        doc.setUid(note.getUid());
        doc.setTitle(StrUtil.blankToDefault(note.getTitle(), ""));
        doc.setContent(StrUtil.blankToDefault(note.getContent(), ""));
        doc.setEmbedding(vec);
        doc.setUpdatedAt(Instant.now().toEpochMilli());

        try {
            noteVectorRepository.save(doc);
        } catch (Exception e) {
            log.warn("写入笔记向量索引失败，noteId={}", noteId, e);
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(value = MQConstants.NOTE_EVENT_EXCHANGE),
                    value = @Queue(value = "note.ai.vector.delete.queue", durable = "true"),
                    key = { MQConstants.NOTE_DELETE_KEY }
            )
    )
    public void handleNoteDelete(Long noteId) {
        if (noteId == null || noteId <= 0) {
            return;
        }
        ensureIndex();
        try {
            noteVectorRepository.deleteById("n_" + noteId);
        } catch (Exception e) {
            log.warn("删除笔记向量索引失败，noteId={}", noteId, e);
        }
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
        // Keep it simple and stable for recall.
        if (StrUtil.isBlank(title)) {
            return content;
        }
        if (StrUtil.isBlank(content)) {
            return title;
        }
        return title + "\n\n" + content;
    }

    private float[] embedOne(String text) {
        if (StrUtil.isBlank(zhipuApiKey)) {
            throw new IllegalStateException("未配置 spring.ai.zhipuai.api-key，无法调用 embedding-3");
        }
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(zhipuApiKey).build();
        EmbeddingCreateParams req = EmbeddingCreateParams.builder()
                .model("embedding-3")
                .input(List.of(text))
                .dimensions(2048)
                .build();
        EmbeddingResponse resp = client.embeddings().createEmbeddings(req);
        if (resp == null || resp.getData() == null || resp.getData().getData() == null || resp.getData().getData().isEmpty()) {
            throw new IllegalStateException("embedding 调用失败：返回为空");
        }
        List<Double> emb = resp.getData().getData().get(0).getEmbedding();
        float[] vec = new float[emb.size()];
        for (int i = 0; i < emb.size(); i++) {
            vec[i] = emb.get(i).floatValue();
        }
        return vec;
    }
}
