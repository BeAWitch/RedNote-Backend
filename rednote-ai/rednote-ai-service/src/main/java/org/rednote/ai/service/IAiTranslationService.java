package org.rednote.ai.service;

import org.rednote.ai.api.dto.AITranslationRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;

/**
 * 统一多语言翻译服务接口
 */
public interface IAiTranslationService {

    /**
     * 提供商标识（如 zhipu, openai, ali）
     * @return 提供商标识
     */
    String provider();

    /**
     * 文本翻译
     * @param request AI翻译请求参数
     * @return AI响应结果
     */
    AIResponseVO translate(AITranslationRequestDTO request);
}
