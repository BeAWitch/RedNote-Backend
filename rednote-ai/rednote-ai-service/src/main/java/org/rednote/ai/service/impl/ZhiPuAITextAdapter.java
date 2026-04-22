package org.rednote.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.constant.AiPromptConstant;
import org.rednote.ai.api.dto.AIRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;
import org.rednote.ai.service.IAiTextGenerationService;
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
@Service("zhiPuAITextAdapter")
@RequiredArgsConstructor
public class ZhiPuAITextAdapter implements IAiTextGenerationService {

    private final ZhiPuAiChatModel chatModel;

    @Override
    public String provider() {
        return "zhipu";
    }

    @Override
    public AIResponseVO generateText(AIRequestDTO aiRequestDTO) {
        log.info("ZhiPu AI 服务调用开始 - 用户ID: {}", aiRequestDTO.getUserId());
        Instant startTime = Instant.now();

        try {
            validateRequest(aiRequestDTO);
            List<Message> messages = buildMessages(aiRequestDTO);
            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatModel.call(prompt);
            return buildSuccessResponse(chatResponse, startTime, aiRequestDTO);
        } catch (IllegalArgumentException e) {
            log.warn("AI 请求参数无效: {}", e.getMessage());
            return buildErrorResponse("INVALID_PARAMETER", e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("AI 服务调用失败", e);
            return buildErrorResponse("AI_SERVICE_ERROR", "AI 服务暂时不可用，请稍后重试", startTime);
        } finally {
            log.info("ZhiPu AI 服务调用结束 - 用户ID: {}", aiRequestDTO.getUserId());
        }
    }

    private void validateRequest(AIRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (StrUtil.isEmpty(request.getMessage())) {
            throw new IllegalArgumentException("用户消息不能为空");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("用户 ID 无效");
        }
    }

    private List<Message> buildMessages(AIRequestDTO request) {
        List<Message> messages = new ArrayList<>();
        String systemPrompt = StrUtil.isNotEmpty(request.getSystemPrompt())
                ? request.getSystemPrompt()
                : AiPromptConstant.GENERAL_ASSISTANT;
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(request.getMessage()));
        return messages;
    }

    private AIResponseVO buildSuccessResponse(ChatResponse chatResponse, Instant startTime, AIRequestDTO request) {
        AIResponseVO response = new AIResponseVO();
        String aiMessage = extractAiMessage(chatResponse);
        response.setMessage(aiMessage);
        response.setSuccess(true);
        response.setResponseTime(calculateResponseTime(startTime));
        log.info("ZhiPu AI 服务调用成功 - 用户ID: {}, 响应时间: {}ms, 内容长度: {}",
                request.getUserId(), response.getResponseTime(), aiMessage.length());
        return response;
    }

    private String extractAiMessage(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null || chatResponse.getResults().isEmpty()) {
            return "抱歉，我暂时无法回答这个问题。";
        }
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        return assistantMessage != null ? assistantMessage.getText() : "未获取到有效回复。";
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
