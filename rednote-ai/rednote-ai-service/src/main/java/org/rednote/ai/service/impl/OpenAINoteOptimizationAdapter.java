package org.rednote.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.constant.AiPromptConstant;
import org.rednote.ai.api.dto.AINoteOptimizeRequestDTO;
import org.rednote.ai.api.service.IAiNoteOptimizationService;
import org.rednote.ai.api.vo.AIResponseVO;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("openAINoteOptimizationAdapter")
@RequiredArgsConstructor
public class OpenAINoteOptimizationAdapter implements IAiNoteOptimizationService {

    private final OpenAiChatModel chatModel;

    @Value("${spring.ai.openai.chat.options.model:gpt-5.2}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;

    @Override
    public String provider() {
        return "openai";
    }

    @Override
    public AIResponseVO optimizeNote(AINoteOptimizeRequestDTO request) {
        log.info("OpenAI 笔记优化服务调用开始 - 用户ID: {}", request.getUserId());
        Instant startTime = Instant.now();

        try {
            validateRequest(request);
            List<Message> messages = buildMessages(request);
            
            // 指定使用配置的模型
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(temperature)
                    .build();

            Prompt prompt = new Prompt(messages, options);
            ChatResponse chatResponse = chatModel.call(prompt);

            return buildSuccessResponse(chatResponse, startTime, request);
        } catch (IllegalArgumentException e) {
            log.warn("AI 笔记优化请求参数无效: {}", e.getMessage());
            return buildErrorResponse("INVALID_PARAMETER", e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("AI 笔记优化服务调用失败", e);
            return buildErrorResponse("AI_SERVICE_ERROR", "AI 服务暂时不可用，请稍后重试: " + e.getMessage(), startTime);
        } finally {
            log.info("OpenAI 笔记优化服务调用结束 - 用户ID: {}", request.getUserId());
        }
    }

    private void validateRequest(AINoteOptimizeRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("用户 ID 无效");
        }
        // 至少要有图片、标题、文案其中之一
        if (CollUtil.isEmpty(request.getImageUrls()) && StrUtil.isEmpty(request.getTitle()) && StrUtil.isEmpty(request.getContent())) {
            throw new IllegalArgumentException("必须提供图片、标题或内容中的至少一项");
        }
    }

    private List<Message> buildMessages(AINoteOptimizeRequestDTO request) {
        List<Message> messages = new ArrayList<>();

        // 构建强大的系统提示词
        String systemPrompt = AiPromptConstant.NOTE_OPTIMIZE_SYSTEM_PROMPT;
        messages.add(new SystemMessage(systemPrompt));

        // 构建用户消息文本内容
        StringBuilder userTextBuilder = new StringBuilder();
        userTextBuilder.append("请帮我优化这篇笔记：\n");
        if (StrUtil.isNotEmpty(request.getTitle())) {
            userTextBuilder.append("【原标题】：").append(request.getTitle()).append("\n");
        }
        if (StrUtil.isNotEmpty(request.getContent())) {
            userTextBuilder.append("【正文内容】：\n").append(request.getContent()).append("\n");
        }
        if (CollUtil.isNotEmpty(request.getTags())) {
            userTextBuilder.append("【原标签】：").append(String.join(", ", request.getTags())).append("\n");
        }

        // 构建多模态图片
        List<Media> mediaList = new ArrayList<>();
        if (CollUtil.isNotEmpty(request.getImageUrls())) {
            for (String imageUrl : request.getImageUrls()) {
                try {
                    mediaList.add(new Media(MimeTypeUtils.IMAGE_JPEG, new java.net.URI(imageUrl)));
                } catch (Exception e) {
                    log.warn("解析图片 URI 失败，已跳过该图片: {}", imageUrl);
                }
            }
        }

        // 构建包含文本和多媒体资源（如果有）的 UserMessage
        if (mediaList.isEmpty()) {
            messages.add(new UserMessage(userTextBuilder.toString()));
        } else {
            messages.add(new UserMessage(userTextBuilder.toString()).mutate().media(mediaList).build());
        }

        return messages;
    }

    private AIResponseVO buildSuccessResponse(ChatResponse chatResponse, Instant startTime, AINoteOptimizeRequestDTO request) {
        AIResponseVO response = new AIResponseVO();
        String aiMessage = extractAiMessage(chatResponse);
        response.setMessage(aiMessage);
        response.setSuccess(true);
        response.setResponseTime(calculateResponseTime(startTime));
        log.info("OpenAI 笔记优化调用成功 - 用户ID: {}, 响应时间: {}ms, 优化内容长度: {}",
                request.getUserId(), response.getResponseTime(), aiMessage.length());
        return response;
    }

    private String extractAiMessage(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null || chatResponse.getResults().isEmpty()) {
            return "抱歉，我暂时无法为您优化文案。";
        }
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        return assistantMessage != null ? assistantMessage.getText() : "未获取到有效的优化结果。";
    }

    private AIResponseVO buildErrorResponse(String errorCode, String errorMessage, Instant startTime) {
        AIResponseVO response = new AIResponseVO();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setError(errorMessage);
        response.setResponseTime(calculateResponseTime(startTime));
        return response;
    }

    private Long calculateResponseTime(Instant startTime) {
        return Duration.between(startTime, Instant.now()).toMillis();
    }
}
