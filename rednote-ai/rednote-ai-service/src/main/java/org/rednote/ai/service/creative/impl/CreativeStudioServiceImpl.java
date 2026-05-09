package org.rednote.ai.service.creative.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;
import ai.z.openapi.service.tools.SearchChatMessage;
import ai.z.openapi.service.tools.WebSearchApiResponse;
import ai.z.openapi.service.tools.WebSearchParamsRequest;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.rednote.ai.api.constant.AiPromptConstant;
import org.rednote.ai.api.dto.CreativeDraftGenerateRequestDTO;
import org.rednote.ai.api.dto.CreativeOutlineGenerateRequestDTO;
import org.rednote.ai.api.dto.CreativeProjectCreateResponseDTO;
import org.rednote.ai.api.dto.CreativeProjectDocDTO;
import org.rednote.ai.api.dto.CreativeProjectDocsUpsertRequestDTO;
import org.rednote.ai.api.vo.CreativeDraftVO;
import org.rednote.ai.api.vo.CreativeOutlineVO;
import org.rednote.ai.api.vo.CreativeProjectDocsUpsertVO;
import org.rednote.ai.api.vo.CreativeSourceVO;
import org.rednote.ai.entity.EsAiUserNoteVector;
import org.rednote.ai.entity.EsCreativeProjectDocChunk;
import org.rednote.ai.repository.EsCreativeProjectDocChunkRepository;
import org.rednote.ai.service.creative.CreativeStudioService;
import org.rednote.common.utils.UserHolder;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * MVP 实现：
 * 1) 文档：PDF -> chunk -> embedding-3 -> ES
 * 2) 历史笔记：向量索引按需构建
 * 3) 联网：search_pro
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreativeStudioServiceImpl implements CreativeStudioService {

    private static final int MAX_SOURCES = 8;
    private static final int PDF_MAX_MB = 10;
    private static final int PDF_MAX_PAGES = 50;

    private static final int DOC_CHUNK_CHAR_LIMIT = 1100;
    private static final int DOC_TOPK_CHUNKS = 12;
    private static final int NOTE_TOPK = 6;
    private static final int WEB_TOPK = 6;

    private static final int EVIDENCE_LINE_MAX_CHARS = 240;
    private static final int EVIDENCE_LINES_PER_SOURCE = 2;

    private static final Pattern CITATION_MARK_PATTERN = Pattern.compile("\\[\\s*S\\d+\\s*]");
    private static final Pattern REF_LINE_PATTERN = Pattern.compile("(?im)^\\s*(参考|引用|来源)\\s*[:：].*$");

    private final StringRedisTemplate stringRedisTemplate;
    private final ElasticsearchOperations elasticsearchOperations;
    private final EsCreativeProjectDocChunkRepository projectDocChunkRepository;
    private final ZhiPuAiChatModel zhiPuAiChatModel;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.zhipuai.api-key:}")
    private String zhipuApiKey;

    @Value("${ai.creative.es.index.project-doc:ai_project_doc_chunk}")
    private String projectDocIndex;

    @Value("${ai.creative.es.index.user-note:ai_user_note_vector}")
    private String userNoteIndex;

    @Value("${ai.creative.web.enabled:false}")
    private boolean webSearchEnabled;

    @Override
    public CreativeProjectCreateResponseDTO createProject() {
        Long userId = UserHolder.getUserId();
        String projectId = "p_" + IdUtil.fastSimpleUUID();
        Map<String, Object> project = new HashMap<>();
        project.put("projectId", projectId);
        project.put("userId", userId);
        project.put("createdAt", Instant.now().toEpochMilli());
        stringRedisTemplate.opsForValue().set(redisKeyProject(projectId), JSON.toJSONString(project));

        CreativeProjectCreateResponseDTO resp = new CreativeProjectCreateResponseDTO();
        resp.setProjectId(projectId);
        return resp;
    }

    @Override
    public CreativeProjectDocsUpsertVO upsertDocs(String projectId, CreativeProjectDocsUpsertRequestDTO request) {
        if (StrUtil.isBlank(projectId)) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        if (request == null || CollUtil.isEmpty(request.getDocs())) {
            throw new IllegalArgumentException("docs 不能为空");
        }

        int indexedDocs = 0;
        int indexedChunks = 0;

        ensureProjectDocIndex();

        for (CreativeProjectDocDTO doc : request.getDocs()) {
            if (doc == null || StrUtil.isBlank(doc.getFileName()) || StrUtil.isBlank(doc.getFileUrl())) {
                continue;
            }
            String text = extractPdfTextWithLimits(doc.getFileUrl());
            if (StrUtil.isBlank(text)) {
                continue;
            }
            List<String> chunks = chunkText(text);
            if (chunks.isEmpty()) {
                continue;
            }

            // 文本嵌入
            List<float[]> embeddings = embedAll(chunks);

            String docId = "d_" + IdUtil.fastSimpleUUID();
            List<EsCreativeProjectDocChunk> toSave = new ArrayList<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = docId + "_c" + i;
                EsCreativeProjectDocChunk chunk = new EsCreativeProjectDocChunk();
                chunk.setId(chunkId);
                chunk.setProjectId(projectId);
                chunk.setDocId(docId);
                chunk.setDocName(doc.getFileName());
                chunk.setChunkId(chunkId);
                chunk.setText(chunks.get(i));
                chunk.setEmbedding(embeddings.get(i));
                chunk.setCreatedAt(Instant.now().toEpochMilli());
                toSave.add(chunk);
                indexedChunks++;
            }
            projectDocChunkRepository.saveAll(toSave);
            indexedDocs++;
        }

        CreativeProjectDocsUpsertVO vo = new CreativeProjectDocsUpsertVO();
        vo.setIndexedDocs(indexedDocs);
        vo.setIndexedChunks(indexedChunks);
        return vo;
    }

    @Override
    public CreativeOutlineVO generateOutline(CreativeOutlineGenerateRequestDTO request) {
        Long userId = UserHolder.getUserId();
        validateOutlineRequest(request);

        // 检索候选
        float[] q = embedAll(List.of(request.getRequirement())).get(0);
        List<Float> qv = toFloatList(q);

        List<SourceWithEvidence> candidates = new ArrayList<>();
        candidates.addAll(retrieveDocSourcesFromEs(request.getProjectId(), qv));
        candidates.addAll(retrieveNoteSourcesFromEs(userId, qv));
        if (webSearchEnabled) {
            candidates.addAll(retrieveWebSources(request.getRequirement()));
        }

        // dedupe and cap to MAX_SOURCES, then assign S1..Sn
        Map<String, SourceWithEvidence> dedup = new LinkedHashMap<>();
        for (SourceWithEvidence c : candidates) {
            if (c == null || c.source == null || StrUtil.isBlank(c.dedupKey)) {
                continue;
            }
            dedup.putIfAbsent(c.dedupKey, c);
            if (dedup.size() >= MAX_SOURCES) {
                break;
            }
        }

        List<SourceWithEvidence> selected = dedup.values().stream().limit(MAX_SOURCES).toList();
        for (int i = 0; i < selected.size(); i++) {
            selected.get(i).source.setId("S" + (i + 1));
        }
        List<CreativeSourceVO> sources = selected.stream().map(s -> s.source).toList();

        String outlineId = "o_" + IdUtil.fastSimpleUUID();

        String system = AiPromptConstant.OUTLINE_SYSTEM_PROMPT;
        String userPrompt = outlineUserPrompt(request, selected);
        Prompt prompt = new Prompt(List.of(new SystemMessage(system), new UserMessage(userPrompt)));
        ChatResponse response = zhiPuAiChatModel.call(prompt);
        String content = Optional.ofNullable(response.getResult()).map(r -> r.getOutput().getText()).orElse("{}");

        CreativeOutlineVO vo = new CreativeOutlineVO();
        vo.setOutlineId(outlineId);
        vo.setSources(sources);
        try {
            String cleanContent = cleanJsonString(content);
            CreativeOutlineVO.Outline outline = JSON.parseObject(cleanContent, CreativeOutlineVO.Outline.class);
            vo.setOutline(outline);
            stringRedisTemplate.opsForValue().set(redisKeyOutline(outlineId), cleanContent);
            stringRedisTemplate.opsForValue().set(redisKeyOutlineSources(outlineId), JSON.toJSONString(sources));
        } catch (Exception e) {
            log.warn("大纲 JSON 解析失败，返回原始文本。userId={}, projectId={}, outlineId={}", userId, request.getProjectId(), outlineId, e);
            // fallback: put raw text into a single section
            CreativeOutlineVO.Outline outline = new CreativeOutlineVO.Outline();
            CreativeOutlineVO.Section section = new CreativeOutlineVO.Section();
            section.setTitle("大纲");
            CreativeOutlineVO.Point p = new CreativeOutlineVO.Point();
            p.setText(content);
            p.setCitations(List.of());
            section.setPoints(List.of(p));
            outline.setSections(List.of(section));
            vo.setOutline(outline);
            stringRedisTemplate.opsForValue().set(redisKeyOutline(outlineId), JSON.toJSONString(outline));
            stringRedisTemplate.opsForValue().set(redisKeyOutlineSources(outlineId), JSON.toJSONString(sources));
        }
        return vo;
    }

    @Override
    public CreativeDraftVO generateDraft(CreativeDraftGenerateRequestDTO request) {
        if (request == null || StrUtil.isBlank(request.getProjectId()) || StrUtil.isBlank(request.getOutlineId())) {
            throw new IllegalArgumentException("projectId/outlineId 不能为空");
        }

        String outlineJson = stringRedisTemplate.opsForValue().get(redisKeyOutline(request.getOutlineId()));
        if (StrUtil.isBlank(outlineJson)) {
            throw new IllegalArgumentException("outlineId 不存在或已被清理");
        }

        String system = AiPromptConstant.DRAFT_SYSTEM_PROMPT;
        String user = draftUserPrompt(outlineJson, request.getExtraRequirement());
        Prompt prompt = new Prompt(List.of(new SystemMessage(system), new UserMessage(user)));
        ChatResponse response = zhiPuAiChatModel.call(prompt);
        String text = Optional.ofNullable(response.getResult()).map(r -> r.getOutput().getText()).orElse("");

        text = cleanupCitations(text);
        CreativeDraftVO vo = new CreativeDraftVO();
        vo.setDraftText(text);
        return vo;
    }

    @Override
    public void cleanup(String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return;
        }
        stringRedisTemplate.delete(redisKeyProject(projectId));
        stringRedisTemplate.delete(redisKeyProjectDocChunks(projectId));

        try {
            // Spring Data derived query; ES side is delete-by-query.
            projectDocChunkRepository.deleteByProjectId(projectId);
        } catch (Exception e) {
            log.warn("清理 ES 项目文档失败，projectId={}", projectId, e);
        }
    }

    /**
     * 清理 JSON 字符串中的 markdown 代码块标记和额外空白字符
     *
     * @param rawContent 原始内容，可能包含 ```json ... ``` 等标记
     * @return 纯净的 JSON 字符串
     */
    private String cleanJsonString(String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return rawContent;
        }

        String cleaned = rawContent.trim();

        // 移除 markdown 代码块标记
        // 匹配 ```json 或 ``` 开头，以及结尾的 ```
        cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "")
                .replaceAll("\\s*```$", "");

        // 移除可能存在的反引号
        cleaned = cleaned.replaceAll("^`+", "")
                .replaceAll("`+$", "");

        // 确保 JSON 以 { 或 [ 开头
        int firstBrace = cleaned.indexOf('{');
        int firstBracket = cleaned.indexOf('[');
        int startIndex = -1;

        if (firstBrace >= 0) {
            startIndex = firstBrace;
        }
        if (firstBracket >= 0 && (startIndex == -1 || firstBracket < startIndex)) {
            startIndex = firstBracket;
        }

        if (startIndex > 0) {
            cleaned = cleaned.substring(startIndex);
        }

        // 确保 JSON 以 } 或 ] 结尾
        int lastBrace = cleaned.lastIndexOf('}');
        int lastBracket = cleaned.lastIndexOf(']');
        int endIndex = -1;

        if (lastBrace >= 0) {
            endIndex = lastBrace + 1;
        }
        if (lastBracket >= 0 && (endIndex == -1 || lastBracket > endIndex)) {
            endIndex = lastBracket + 1;
        }

        if (endIndex > 0 && endIndex < cleaned.length()) {
            cleaned = cleaned.substring(0, endIndex);
        }

        return cleaned.trim();
    }

    private void validateOutlineRequest(CreativeOutlineGenerateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (StrUtil.isBlank(request.getProjectId())) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        if (StrUtil.isBlank(request.getRequirement())) {
            throw new IllegalArgumentException("requirement 不能为空");
        }
    }

    private String extractPdfTextWithLimits(String fileUrl) {
        byte[] bytes = downloadFileWithLimit(fileUrl, PDF_MAX_MB * 1024L * 1024L);
        try (PDDocument document = PDDocument.load(bytes)) {
            if (document.getNumberOfPages() > PDF_MAX_PAGES) {
                throw new IllegalArgumentException("PDF 页数超过限制(" + PDF_MAX_PAGES + ")");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return StrUtil.trimToEmpty(text);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("PDF 解析失败: " + e.getMessage(), e);
        }
    }

    private byte[] downloadFileWithLimit(String url, long maxBytes) {
        return restTemplate.execute(url, HttpMethod.GET, req -> {
            // do nothing
        }, response -> {
            long contentLength = response.getHeaders().getContentLength();
            if (contentLength > 0 && contentLength > maxBytes) {
                throw new IllegalArgumentException("文件大小超过限制(" + PDF_MAX_MB + "MB)");
            }
            byte[] data = StreamUtils.copyToByteArray(response.getBody());
            if (data.length > maxBytes) {
                throw new IllegalArgumentException("文件大小超过限制(" + PDF_MAX_MB + "MB)");
            }
            return data;
        });
    }

    private List<String> chunkText(String text) {
        // 优先使用段落分块，然后使用字符分块
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        String[] paras = normalized.split("\n{2,}");
        List<String> chunks = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (String p : paras) {
            String para = StrUtil.trimToEmpty(p);
            if (para.isEmpty()) {
                continue;
            }
            if (buf.isEmpty()) {
                buf.append(para);
            } else if (buf.length() + 2 + para.length() <= DOC_CHUNK_CHAR_LIMIT) {
                buf.append("\n\n").append(para);
            } else {
                chunks.add(buf.toString());
                buf.setLength(0);
                if (para.length() <= DOC_CHUNK_CHAR_LIMIT) {
                    buf.append(para);
                } else {
                    // hard split
                    int idx = 0;
                    while (idx < para.length()) {
                        int end = Math.min(idx + DOC_CHUNK_CHAR_LIMIT, para.length());
                        chunks.add(para.substring(idx, end));
                        idx = end;
                    }
                }
            }
        }
        if (!buf.isEmpty()) {
            chunks.add(buf.toString());
        }
        return chunks;
    }

    private List<float[]> embedAll(List<String> texts) {
        if (StrUtil.isBlank(zhipuApiKey)) {
            throw new IllegalStateException("未配置 spring.ai.zhipuai.api-key，无法调用 embedding-3");
        }
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(zhipuApiKey).build();

        List<float[]> vectors = new ArrayList<>(texts.size());
        int i = 0;
        while (i < texts.size()) {
            int end = Math.min(i + 64, texts.size());
            List<String> batch = texts.subList(i, end);
            EmbeddingCreateParams req = EmbeddingCreateParams.builder()
                    .model("embedding-3")
                    .input(batch)
                    .dimensions(2048)
                    .build();
            EmbeddingResponse resp = client.embeddings().createEmbeddings(req);
            if (resp == null || resp.getData() == null || resp.getData().getData() == null) {
                throw new IllegalStateException("embedding 调用失败：返回为空");
            }
            resp.getData().getData().forEach(d -> {
                List<Double> emb = d.getEmbedding();
                float[] vec = new float[emb.size()];
                for (int k = 0; k < emb.size(); k++) {
                    vec[k] = emb.get(k).floatValue();
                }
                vectors.add(vec);
            });
            i = end;
        }
        return vectors;
    }

    private static final class SourceWithEvidence {
        private final CreativeSourceVO source;
        private final List<String> evidence;
        private final String dedupKey;

        private SourceWithEvidence(CreativeSourceVO source, List<String> evidence, String dedupKey) {
            this.source = source;
            this.evidence = evidence;
            this.dedupKey = dedupKey;
        }
    }

    private List<Float> toFloatList(float[] vector) {
        List<Float> qv = new ArrayList<>(vector.length);
        for (float v : vector) {
            qv.add(v);
        }
        return qv;
    }

    private String normalizeEvidence(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        String s = text.replace("\r\n", "\n").replace("\r", "\n");
        s = s.replaceAll("[ \t]+", " ");
        s = s.replaceAll("\n{3,}", "\n\n");
        s = s.trim();
        if (s.length() <= EVIDENCE_LINE_MAX_CHARS) {
            return s;
        }
        return s.substring(0, EVIDENCE_LINE_MAX_CHARS) + "...";
    }

    private List<SourceWithEvidence> retrieveDocSourcesFromEs(String projectId, List<Float> queryVector) {
        if (StrUtil.isBlank(projectId) || CollUtil.isEmpty(queryVector)) {
            return List.of();
        }

        ensureProjectDocIndex();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.scriptScore(ss -> ss
                        .query(sq -> sq.bool(b -> b
                                .filter(f -> f.term(t -> t.field("projectId").value(projectId)))
                                .filter(f -> f.exists(e -> e.field("embedding")))
                        ))
                        .script(s -> s
                                .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                                .params("query_vector", JsonData.of(queryVector))
                        )
                ))
                .withPageable(PageRequest.of(0, DOC_TOPK_CHUNKS))
                .build();

        SearchHits<EsCreativeProjectDocChunk> hits = elasticsearchOperations.search(nativeQuery, EsCreativeProjectDocChunk.class);
        if (hits.isEmpty()) {
            return List.of();
        }

        // 按文档名称进行汇总；为每个文档保留几段关键内容作为证据。
        Map<String, List<String>> evidenceByDoc = new LinkedHashMap<>();
        for (SearchHit<EsCreativeProjectDocChunk> hit : hits.getSearchHits()) {
            EsCreativeProjectDocChunk c = hit.getContent();
            if (StrUtil.isBlank(c.getDocName())) {
                continue;
            }
            String docName = c.getDocName();
            evidenceByDoc.putIfAbsent(docName, new ArrayList<>());
            List<String> ev = evidenceByDoc.get(docName);
            if (ev.size() < EVIDENCE_LINES_PER_SOURCE) {
                String line = normalizeEvidence(c.getText());
                if (StrUtil.isNotBlank(line)) {
                    ev.add(line);
                }
            }
            if (evidenceByDoc.size() >= MAX_SOURCES) {
                break;
            }
        }

        return evidenceByDoc.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(MAX_SOURCES)
                .map(e -> {
                    CreativeSourceVO vo = new CreativeSourceVO();
                    vo.setType("doc");
                    vo.setTitle(e.getKey());
                    return new SourceWithEvidence(vo, e.getValue(), "doc:" + e.getKey());
                })
                .toList();
    }

    private List<SourceWithEvidence> retrieveNoteSourcesFromEs(Long userId, List<Float> queryVector) {
        if (userId == null || userId <= 0 || CollUtil.isEmpty(queryVector)) {
            return List.of();
        }

        ensureUserNoteIndex();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.scriptScore(ss -> ss
                        .query(sq -> sq.bool(b -> b
                                .filter(f -> f.term(t -> t.field("uid").value(userId)))
                                .filter(f -> f.exists(e -> e.field("embedding")))
                        ))
                        .script(s -> s
                                .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                                .params("query_vector", JsonData.of(queryVector))
                        )
                ))
                .withPageable(PageRequest.of(0, NOTE_TOPK))
                .build();

        SearchHits<EsAiUserNoteVector> hits = elasticsearchOperations.search(nativeQuery, EsAiUserNoteVector.class);
        if (hits.isEmpty()) {
            return List.of();
        }

        Map<Long, SourceWithEvidence> byNoteId = new LinkedHashMap<>();
        for (SearchHit<EsAiUserNoteVector> hit : hits.getSearchHits()) {
            EsAiUserNoteVector c = hit.getContent();
            if (c.getNoteId() == null) {
                continue;
            }
            Long noteId = c.getNoteId();
            byNoteId.computeIfAbsent(noteId, nid -> {
                CreativeSourceVO vo = new CreativeSourceVO();
                vo.setType("note");
                vo.setTitle(StrUtil.blankToDefault(c.getTitle(), "笔记" + nid));
                List<String> ev = new ArrayList<>();
                String line = normalizeEvidence(c.getContent());
                if (StrUtil.isNotBlank(line)) {
                    ev.add(line);
                }
                return new SourceWithEvidence(vo, ev, "note:" + nid);
            });
            if (byNoteId.size() >= MAX_SOURCES) {
                break;
            }
        }

        return byNoteId.values().stream().limit(MAX_SOURCES).toList();
    }

    private void ensureProjectDocIndex() {
        try {
            var ops = elasticsearchOperations.indexOps(EsCreativeProjectDocChunk.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping();
            }
        } catch (Exception e) {
            // ES 初始化失败，暂时允许继续运行
            log.warn("初始化 ES 索引失败: index={}, entity={}", projectDocIndex, EsCreativeProjectDocChunk.class.getSimpleName(), e);
        }
    }

    private void ensureUserNoteIndex() {
        try {
            var ops = elasticsearchOperations.indexOps(EsAiUserNoteVector.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping();
            }
        } catch (Exception e) {
            log.warn("初始化 ES 索引失败: index={}, entity={}", userNoteIndex, EsAiUserNoteVector.class.getSimpleName(), e);
        }
    }

    private List<SourceWithEvidence> retrieveWebSources(String queryText) {
        if (StrUtil.isBlank(queryText)) {
            return List.of();
        }
        if (StrUtil.isBlank(zhipuApiKey)) {
            return List.of();
        }

        try {
            ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(zhipuApiKey).build();

            SearchChatMessage msg = SearchChatMessage.builder()
                    .role("user")
                    .content(queryText)
                    .build();

            WebSearchParamsRequest req = WebSearchParamsRequest.builder()
                    .model("search_pro")
                    .stream(false)
                    .messages(List.of(msg))
                    .recentDays(30)
                    .build();

            WebSearchApiResponse resp = client.webSearch().createWebSearchPro(req);
            if (resp == null || !resp.isSuccess() || resp.getData() == null || CollUtil.isEmpty(resp.getData().getChoices())) {
                return List.of();
            }
            var choice0 = resp.getData().getChoices().get(0);
            if (choice0 == null || choice0.getMessage() == null || CollUtil.isEmpty(choice0.getMessage().getToolCalls())) {
                return List.of();
            }

            Map<String, SourceWithEvidence> byUrl = new LinkedHashMap<>();
            for (var tc : choice0.getMessage().getToolCalls()) {
                if (tc == null || CollUtil.isEmpty(tc.getSearchResult())) {
                    continue;
                }
                tc.getSearchResult().forEach(r -> {
                    if (r == null || StrUtil.isBlank(r.getLink()) || StrUtil.isBlank(r.getTitle())) {
                        return;
                    }
                    if (byUrl.size() >= WEB_TOPK) {
                        return;
                    }
                    byUrl.computeIfAbsent(r.getLink(), u -> {
                        CreativeSourceVO vo = new CreativeSourceVO();
                        vo.setType("web");
                        vo.setTitle(r.getTitle());
                        vo.setUrl(u);
                        List<String> ev = new ArrayList<>();
                        // Try to extract snippet-like fields without depending on SDK getters.
                        try {
                            Map<String, Object> m = JSON.parseObject(JSON.toJSONString(r));
                            Object snippet = m.get("content");
                            if (snippet == null) snippet = m.get("snippet");
                            if (snippet == null) snippet = m.get("summary");
                            if (snippet == null) snippet = m.get("desc");
                            String line = normalizeEvidence(snippet == null ? "" : String.valueOf(snippet));
                            if (StrUtil.isNotBlank(line)) {
                                ev.add(line);
                            }
                        } catch (Exception ignore) {
                            // best-effort
                        }
                        return new SourceWithEvidence(vo, ev, "web:" + u);
                    });
                });
                if (byUrl.size() >= WEB_TOPK) {
                    break;
                }
            }

            return byUrl.values().stream()
                    .limit(WEB_TOPK)
                    .toList();
        } catch (Exception e) {
            log.warn("web_search(search_pro) 调用失败", e);
            return List.of();
        }
    }

    private String outlineUserPrompt(CreativeOutlineGenerateRequestDTO req, List<SourceWithEvidence> sources) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户需求：\n").append(req.getRequirement()).append("\n\n");
        if (StrUtil.isNotBlank(req.getCategory())) {
            sb.append("分类：").append(req.getCategory()).append("\n");
        }
        if (StrUtil.isNotBlank(req.getStyle())) {
            sb.append("风格/人设：").append(req.getStyle()).append("\n");
        }
        if (StrUtil.isNotBlank(req.getTone())) {
            sb.append("语气：").append(req.getTone()).append("\n");
        }
        sb.append("\n可用来源：\n");
        for (SourceWithEvidence sw : sources) {
            CreativeSourceVO s = sw.source;
            sb.append("- ").append(s.getId() == null ? "" : s.getId()).append(" ")
                    .append("[").append(s.getType()).append("] ")
                    .append(s.getTitle());
            if (StrUtil.isNotBlank(s.getUrl())) {
                sb.append(" (").append(s.getUrl()).append(")");
            }
            sb.append("\n");
        }

        sb.append("\n来源内容摘录（用于引用判断）：\n");
        for (SourceWithEvidence sw : sources) {
            CreativeSourceVO s = sw.source;
            sb.append("[").append(s.getId()).append("] ").append(s.getTitle()).append("\n");
            List<String> ev = sw.evidence == null ? List.of() : sw.evidence;
            if (ev.isEmpty()) {
                sb.append("- （无摘录）\n");
                continue;
            }
            int n = 0;
            for (String line : ev) {
                if (StrUtil.isBlank(line)) {
                    continue;
                }
                sb.append("- ").append(line).append("\n");
                n++;
                if (n >= EVIDENCE_LINES_PER_SOURCE) {
                    break;
                }
            }
            if (n == 0) {
                sb.append("- （无摘录）\n");
            }
        }

        sb.append("\n请基于以上摘录生成大纲，并在每个要点标注 citations（S1..S8）。\n");
        return sb.toString();
    }

    private String draftUserPrompt(String outlineJson, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append("大纲(JSON)：\n").append(outlineJson).append("\n\n");
        if (StrUtil.isNotBlank(extra)) {
            sb.append("补充要求：\n").append(extra).append("\n\n");
        }
        sb.append("请生成一篇排版精美、网感强的小红书正文。\n");
        return sb.toString();
    }

    private String cleanupCitations(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        String cleaned = CITATION_MARK_PATTERN.matcher(text).replaceAll("");
        cleaned = REF_LINE_PATTERN.matcher(cleaned).replaceAll("");
        return cleaned.trim();
    }

    private String redisKeyProject(String projectId) {
        return "creative:project:" + projectId;
    }

    private String redisKeyProjectDocChunks(String projectId) {
        return "creative:project:docs:" + projectId;
    }

    private String redisKeyOutline(String outlineId) {
        return "creative:outline:" + outlineId;
    }

    private String redisKeyOutlineSources(String outlineId) {
        return "creative:outline:sources:" + outlineId;
    }
}
