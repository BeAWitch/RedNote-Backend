package org.rednote.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.constant.AiPromptConstant;
import org.rednote.ai.api.dto.AITranslationRequestDTO;
import org.rednote.ai.service.IAiTranslationService;
import org.rednote.ai.api.vo.AIResponseVO;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("zhiPuTranslationAdapter")
@RequiredArgsConstructor
public class ZhiPuTranslationAdapter implements IAiTranslationService {

    private final ZhiPuAiChatModel chatModel;

    @Override
    public String provider() {
        return "zhipu";
    }

    @Override
    public AIResponseVO translate(AITranslationRequestDTO request) {
        log.info("ZhiPu 文本翻译服务调用开始 - 用户ID: {}", request.getUserId());
        Instant startTime = Instant.now();

        try {
            validateRequest(request);
            List<Message> messages = buildMessages(request);

            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatModel.call(prompt);

            return buildSuccessResponse(chatResponse, startTime, request);
        } catch (IllegalArgumentException e) {
            log.warn("AI 文本翻译请求参数无效: {}", e.getMessage());
            return buildErrorResponse("INVALID_PARAMETER", e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("AI 文本翻译服务调用失败", e);
            return buildErrorResponse("AI_SERVICE_ERROR", "AI 服务暂时不可用，请稍后重试: " + e.getMessage(), startTime);
        } finally {
            log.info("ZhiPu 文本翻译服务调用结束 - 用户ID: {}", request.getUserId());
        }
    }

    private void validateRequest(AITranslationRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("用户 ID 无效");
        }
        if (StrUtil.isEmpty(request.getOriginalText())) {
            throw new IllegalArgumentException("需要翻译的原文不能为空");
        }
        if (StrUtil.isEmpty(request.getTargetLanguage())) {
            throw new IllegalArgumentException("目标语言不能为空");
        }
    }

    private List<Message> buildMessages(AITranslationRequestDTO request) {
        List<Message> messages = new ArrayList<>();

        String systemPrompt = AiPromptConstant.TRANSLATOR;
        messages.add(new SystemMessage(systemPrompt));

        String userMessageContent = String.format("请将以下内容翻译成【%s】。\n\n【原文】：\n%s",
                request.getTargetLanguage(), request.getOriginalText());
        messages.add(new UserMessage(userMessageContent));

        return messages;
    }

    private AIResponseVO buildSuccessResponse(ChatResponse chatResponse, Instant startTime, AITranslationRequestDTO request) {
        AIResponseVO response = new AIResponseVO();
        String aiMessage = extractAiMessage(chatResponse);
        response.setMessage(aiMessage);
        response.setSuccess(true);
        response.setResponseTime(calculateResponseTime(startTime));
        log.info("ZhiPu 文本翻译调用成功 - 用户ID: {}, 响应时间: {}ms, 翻译内容长度: {}",
                request.getUserId(), response.getResponseTime(), aiMessage.length());
        return response;
    }

    private String extractAiMessage(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null || chatResponse.getResults().isEmpty()) {
            return "抱歉，我暂时无法为您翻译文本。";
        }
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        return assistantMessage != null ? assistantMessage.getText() : "未获取到有效的翻译结果。";
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
