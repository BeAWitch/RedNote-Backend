package org.rednote.ai.service.creative.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
import org.rednote.ai.entity.EsCreativeProjectDocChunk;
import org.rednote.ai.repository.EsCreativeProjectDocChunkRepository;
import org.rednote.ai.service.creative.CreativeStudioService;
import org.rednote.ai.tool.CreativeTools;
import org.rednote.common.utils.UserHolder;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
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

    private static final Pattern CITATION_MARK_PATTERN = Pattern.compile("\\[\\s*[SNDW]\\d+\\s*]");
    private static final Pattern REF_LINE_PATTERN = Pattern.compile("(?im)^\\s*(参考|引用|来源)\\s*[:：].*$");

    private final StringRedisTemplate stringRedisTemplate;
    private final ElasticsearchOperations elasticsearchOperations;
    private final EsCreativeProjectDocChunkRepository projectDocChunkRepository;
    private final ZhiPuAiChatModel zhiPuAiChatModel;
    private final CreativeTools creativeTools;
    private final ToolSearchToolCallAdvisor toolSearchAdvisor;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.zhipuai.api-key:}")
    private String zhipuApiKey;

    @Value("${ai.creative.es.index.project-doc:ai_project_doc_chunk}")
    private String projectDocIndex;

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
        UserHolder.getUserId();
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

        String outlineId = "o_" + IdUtil.fastSimpleUUID();
        creativeTools.beginSession(request.getProjectId());
        try {
            ChatClient chatClient = ChatClient.builder(zhiPuAiChatModel)
                    .defaultTools(creativeTools)
                    .defaultAdvisors(toolSearchAdvisor)
                    .build();

            String system = AiPromptConstant.OUTLINE_SYSTEM_PROMPT;
            String user = outlineUserRequirement(request);
            String content = chatClient.prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .content();
            if (StrUtil.isBlank(content)) {
                content = "{}";
            }

            LinkedHashMap<String, CreativeSourceVO> toolSources = creativeTools.endSession();
            // Assign S1..Sn in insertion order
            List<CreativeSourceVO> sources = new ArrayList<>(toolSources.values());
            Map<String, String> refToSid = new LinkedHashMap<>();
            for (int i = 0; i < sources.size() && i < MAX_SOURCES; i++) {
                String sid = "S" + (i + 1);
                CreativeSourceVO sv = sources.get(i);
                sv.setId(sid);
                // 从 toolSources 反查 refId 建立映射
                String refId = null;
                for (var entry : toolSources.entrySet()) {
                    if (entry.getValue() == sv) {
                        refId = entry.getKey();
                        break;
                    }
                }
                if (refId != null) {
                    refToSid.put(refId, sid);
                }
            }

            CreativeOutlineVO vo = new CreativeOutlineVO();
            vo.setOutlineId(outlineId);
            vo.setSources(sources);
            try {
                String cleanContent = cleanJsonString(content);
                CreativeOutlineVO.Outline outline = JSON.parseObject(cleanContent, CreativeOutlineVO.Outline.class);
                remapCitations(outline, refToSid);
                vo.setOutline(outline);
                stringRedisTemplate.opsForValue().set(redisKeyOutline(outlineId), JSON.toJSONString(outline));
                stringRedisTemplate.opsForValue().set(redisKeyOutlineSources(outlineId), JSON.toJSONString(sources));
            } catch (Exception e) {
                log.warn("大纲 JSON 解析失败，返回原始文本。userId={}, projectId={}, outlineId={}", userId, request.getProjectId(), outlineId, e);
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
        } finally {
            creativeTools.clearSession();
        }
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
        UserHolder.getUserId();
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
                    .build();
            EmbeddingResponse resp = client.embeddings().createEmbeddings(req);
            if (resp == null || !resp.isSuccess() || resp.getData() == null || resp.getData().getData() == null) {
                String errMsg = (resp != null && resp.getError() != null)
                        ? resp.getError().getCode() + ": " + resp.getError().getMessage()
                        : "返回为空";
                log.warn("embedding 调用失败: {}，参数：{}", errMsg, JSON.toJSONString(req));
                throw new IllegalStateException("embedding 调用失败：" + errMsg);
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

    private void ensureProjectDocIndex() {
        try {
            var ops = elasticsearchOperations.indexOps(EsCreativeProjectDocChunk.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping();
            }
        } catch (Exception e) {
            log.warn("初始化 ES 索引失败: index={}, entity={}", projectDocIndex, EsCreativeProjectDocChunk.class.getSimpleName(), e);
        }
    }

    private String outlineUserRequirement(CreativeOutlineGenerateRequestDTO req) {
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
        sb.append("\n当前项目ID：").append(req.getProjectId());
        sb.append("\n请先使用工具搜索相关资料，获得素材后生成大纲。");
        return sb.toString();
    }

    private void remapCitations(CreativeOutlineVO.Outline outline, Map<String, String> refToSid) {
        if (refToSid.isEmpty() || outline == null || CollUtil.isEmpty(outline.getSections())) {
            return;
        }
        for (CreativeOutlineVO.Section section : outline.getSections()) {
            if (section == null || CollUtil.isEmpty(section.getPoints())) {
                continue;
            }
            for (CreativeOutlineVO.Point point : section.getPoints()) {
                if (point == null || CollUtil.isEmpty(point.getCitations())) {
                    continue;
                }
                List<String> remapped = new ArrayList<>();
                for (String c : point.getCitations()) {
                    String mapped = refToSid.getOrDefault(c.trim(), c.trim());
                    remapped.add(mapped);
                }
                point.setCitations(remapped);
            }
        }
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
