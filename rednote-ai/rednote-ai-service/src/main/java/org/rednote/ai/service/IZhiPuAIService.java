package org.rednote.ai.service;

import org.rednote.ai.api.dto.AIRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;

/**
 * 智谱 AI 服务
 **/
public interface IZhiPuAIService {

    /**
     * 生成
     *
     * @param aiRequestDTO 请求参数
     * @return 响应结果
     */
    AIResponseVO generate(AIRequestDTO aiRequestDTO);
}
