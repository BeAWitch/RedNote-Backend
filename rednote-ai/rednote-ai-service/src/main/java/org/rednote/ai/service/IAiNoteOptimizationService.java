package org.rednote.ai.service;

import org.rednote.ai.api.dto.AINoteOptimizeRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;

/**
 * 统一多模态笔记优化服务接口
 */
public interface IAiNoteOptimizationService {

    /**
     * 提供商标识（如 zhipu, openai, ali）
     * @return 提供商标识
     */
    String provider();

    /**
     * 优化笔记（支持图文多模态）
     * @param request AI多模态请求参数
     * @return AI响应结果
     */
    AIResponseVO optimizeNote(AINoteOptimizeRequestDTO request);
}
