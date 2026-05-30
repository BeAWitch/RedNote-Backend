package org.rednote.ai.tool;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;
import ai.z.openapi.service.tools.SearchChatMessage;
import ai.z.openapi.service.tools.WebSearchApiResponse;
import ai.z.openapi.service.tools.WebSearchParamsRequest;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.vo.CreativeSourceVO;
import org.rednote.ai.entity.EsAiUserNoteVector;
import org.rednote.ai.entity.EsCreativeProjectDocChunk;
import org.rednote.common.utils.UserHolder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CreativeTools {

    private static final int DOC_TOPK_CHUNKS = 12;
    private static final int NOTE_TOPK = 8;
    private static final int WEB_TOPK = 6;
    private static final int SNIPPET_MAX_CHARS = 240;

    private final ThreadLocal<Session> sessionHolder = new ThreadLocal<>();

    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${spring.ai.zhipuai.api-key:}")
    private String zhipuApiKey;

    public CreativeTools(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    private static class Session {
        String projectId;
        final LinkedHashMap<String, CreativeSourceVO> sources = new LinkedHashMap<>();
        int docCounter;
        int noteCounter;
        int webCounter;
    }

    public void beginSession(String projectId) {
        Session s = new Session();
        s.projectId = projectId;
        sessionHolder.set(s);
    }

    public LinkedHashMap<String, CreativeSourceVO> endSession() {
        Session s = sessionHolder.get();
        return s != null ? s.sources : new LinkedHashMap<>();
    }

    public void clearSession() {
        sessionHolder.remove();
    }

    @Tool(description = "语义搜索当前创作项目中已上传的PDF文档，返回相关段落及出处。用于查找用户上传的参考资料")
    public List<DocRef> searchProjectDocs(
            @ToolParam(description = "项目ID", required = true) String projectId,
            @ToolParam(description = "语义搜索查询词", required = true) String query,
            @ToolParam(description = "返回条数，默认5，最大10", required = false) Integer limit) {
        if (StrUtil.isBlank(projectId) || StrUtil.isBlank(query)) {
            return List.of();
        }
        int n = limit != null ? Math.min(limit, DOC_TOPK_CHUNKS) : 5;
        n = Math.max(n, 1);

        ensureDocIndex();
        float[] q = embedOne(query);
        List<Float> qv = toFloatList(q);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(qb -> qb.scriptScore(ss -> ss
                        .query(sq -> sq.bool(b -> b
                                .filter(f -> f.term(t -> t.field("projectId").value(projectId)))
                                .filter(f -> f.exists(e -> e.field("embedding")))
                        ))
                        .script(s -> s
                                .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                                .params("query_vector", JsonData.of(qv))
                        )
                ))
                .withPageable(PageRequest.of(0, n))
                .build();

        SearchHits<EsCreativeProjectDocChunk> hits = elasticsearchOperations.search(nativeQuery, EsCreativeProjectDocChunk.class);
        if (hits.isEmpty()) {
            return List.of();
        }

        List<DocRef> results = new ArrayList<>();
        Session session = sessionHolder.get();
        for (SearchHit<EsCreativeProjectDocChunk> hit : hits.getSearchHits()) {
            EsCreativeProjectDocChunk c = hit.getContent();
            if (c == null || StrUtil.isBlank(c.getDocName())) {
                continue;
            }
            session.docCounter++;
            String refId = "D" + session.docCounter;
            String snippet = normalizeText(c.getText(), SNIPPET_MAX_CHARS);
            results.add(new DocRef(refId, c.getDocName(), snippet));
            session.sources.putIfAbsent(refId, toSource("doc", c.getDocName(), null));
            if (results.size() >= n) {
                break;
            }
        }
        return results;
    }

    @Tool(description = "语义搜索当前用户已发布的历史笔记，返回相关笔记标题和内容摘要。用于查找用户曾经创作过的相关内容")
    public List<NoteRef> searchUserNotes(
            @ToolParam(description = "语义搜索查询词", required = true) String query,
            @ToolParam(description = "返回条数，默认5，最大10", required = false) Integer limit) {
        if (StrUtil.isBlank(query)) {
            return List.of();
        }
        Long userId = UserHolder.getUserId();
        if (userId == null || userId <= 0) {
            return List.of();
        }
        int n = limit != null ? Math.min(limit, NOTE_TOPK) : 5;
        n = Math.max(n, 1);

        ensureNoteIndex();
        float[] q = embedOne(query);
        List<Float> qv = toFloatList(q);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(qb -> qb.scriptScore(ss -> ss
                        .query(sq -> sq.bool(b -> b
                                .filter(f -> f.term(t -> t.field("uid").value(userId)))
                                .filter(f -> f.exists(e -> e.field("embedding")))
                        ))
                        .script(s -> s
                                .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                                .params("query_vector", JsonData.of(qv))
                        )
                ))
                .withPageable(PageRequest.of(0, n))
                .build();

        SearchHits<EsAiUserNoteVector> hits = elasticsearchOperations.search(nativeQuery, EsAiUserNoteVector.class);
        if (hits.isEmpty()) {
            return List.of();
        }

        List<NoteRef> results = new ArrayList<>();
        Session session = sessionHolder.get();
        for (SearchHit<EsAiUserNoteVector> hit : hits.getSearchHits()) {
            EsAiUserNoteVector c = hit.getContent();
            if (c == null || c.getNoteId() == null) {
                continue;
            }
            session.noteCounter++;
            String refId = "N" + session.noteCounter;
            String title = StrUtil.blankToDefault(c.getTitle(), "笔记" + c.getNoteId());
            String snippet = normalizeText(c.getContent(), SNIPPET_MAX_CHARS);
            results.add(new NoteRef(refId, c.getNoteId(), title, snippet));
            session.sources.putIfAbsent(refId, toSource("note", title, null));
            if (results.size() >= n) {
                break;
            }
        }
        return results;
    }

    @Tool(description = "联网搜索最新信息，返回网页标题、URL和摘要。用于获取实时资讯和最新资料")
    public List<WebRef> searchWeb(
            @ToolParam(description = "搜索关键词", required = true) String query) {
        if (StrUtil.isBlank(query) || StrUtil.isBlank(zhipuApiKey)) {
            return List.of();
        }

        try {
            ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(zhipuApiKey).build();
            SearchChatMessage msg = SearchChatMessage.builder()
                    .role("user")
                    .content(query)
                    .build();
            WebSearchParamsRequest req = WebSearchParamsRequest.builder()
                    .model("web-search-pro")
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

            List<WebRef> results = new ArrayList<>();
            Session session = sessionHolder.get();
            for (var tc : choice0.getMessage().getToolCalls()) {
                if (tc == null || CollUtil.isEmpty(tc.getSearchResult())) {
                    continue;
                }
                tc.getSearchResult().forEach(r -> {
                    if (r == null || StrUtil.isBlank(r.getLink()) || StrUtil.isBlank(r.getTitle())) {
                        return;
                    }
                    if (results.size() >= WEB_TOPK) {
                        return;
                    }
                    session.webCounter++;
                    String refId = "W" + session.webCounter;
                    String snippet = "";
                    try {
                        Map<String, Object> m = JSON.parseObject(JSON.toJSONString(r));
                        Object s = m.get("content");
                        if (s == null) s = m.get("snippet");
                        if (s == null) s = m.get("summary");
                        if (s == null) s = m.get("desc");
                        snippet = normalizeText(s == null ? "" : String.valueOf(s), SNIPPET_MAX_CHARS);
                    } catch (Exception ignore) {
                    }
                    results.add(new WebRef(refId, r.getTitle(), r.getLink(), snippet));
                    session.sources.putIfAbsent(refId, toSource("web", r.getTitle(), r.getLink()));
                });
                if (results.size() >= WEB_TOPK) {
                    break;
                }
            }
            return results;
        } catch (Exception e) {
            log.warn("web_search(search_pro) 失败", e);
            return List.of();
        }
    }

    private CreativeSourceVO toSource(String type, String title, String url) {
        CreativeSourceVO vo = new CreativeSourceVO();
        vo.setType(type);
        vo.setTitle(title);
        vo.setUrl(url);
        return vo;
    }

    private float[] embedOne(String text) {
        if (StrUtil.isBlank(zhipuApiKey)) {
            throw new IllegalStateException("未配置 spring.ai.zhipuai.api-key");
        }
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().apiKey(zhipuApiKey).build();
        EmbeddingCreateParams req = EmbeddingCreateParams.builder()
                .model("embedding-3")
                .input(List.of(text))
                .build();
        EmbeddingResponse resp = client.embeddings().createEmbeddings(req);
        if (resp == null || !resp.isSuccess() || resp.getData() == null || resp.getData().getData() == null || resp.getData().getData().isEmpty()) {
            String errMsg = (resp != null && resp.getError() != null)
                    ? resp.getError().getCode() + ": " + resp.getError().getMessage()
                    : "返回为空";
            throw new IllegalStateException("embedding 返回为空：" + errMsg);
        }
        List<Double> emb = resp.getData().getData().get(0).getEmbedding();
        float[] vec = new float[emb.size()];
        for (int i = 0; i < emb.size(); i++) {
            vec[i] = emb.get(i).floatValue();
        }
        return vec;
    }

    private List<Float> toFloatList(float[] vector) {
        List<Float> qv = new ArrayList<>(vector.length);
        for (float v : vector) {
            qv.add(v);
        }
        return qv;
    }

    private String normalizeText(String text, int maxChars) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        String s = text.replace("\r\n", "\n").replace("\r", "\n");
        s = s.replaceAll("[ \t]+", " ");
        s = s.replaceAll("\n{3,}", "\n\n");
        s = s.trim();
        if (s.length() <= maxChars) {
            return s;
        }
        return s.substring(0, maxChars) + "...";
    }

    private void ensureDocIndex() {
        try {
            var ops = elasticsearchOperations.indexOps(EsCreativeProjectDocChunk.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping();
            }
        } catch (Exception e) {
            log.warn("初始化 doc chunk 索引失败", e);
        }
    }

    private void ensureNoteIndex() {
        try {
            var ops = elasticsearchOperations.indexOps(EsAiUserNoteVector.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping();
            }
        } catch (Exception e) {
            log.warn("初始化 note vector 索引失败", e);
        }
    }

    public record DocRef(String refId, String docName, String snippet) {
    }

    public record NoteRef(String refId, Long noteId, String title, String snippet) {
    }

    public record WebRef(String refId, String title, String url, String snippet) {
    }
}
