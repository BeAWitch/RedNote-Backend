package org.rednote.ai.api.service;

import org.rednote.ai.api.dto.AIRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;

/**
 * 统一文案生成服务接口
 */
public interface IAiTextGenerationService {
    
    /**
     * 提供商标识（如 zhipu, openai, ali）
     * @return 提供商标识
     */
    String provider();

    /**
     * 生成文本
     * @param request AI请求参数
     * @return AI响应结果
     */
    AIResponseVO generateText(AIRequestDTO request);
}
