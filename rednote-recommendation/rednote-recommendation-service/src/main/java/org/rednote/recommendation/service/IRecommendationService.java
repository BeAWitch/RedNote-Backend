package org.rednote.recommendation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.recommendation.api.entity.UserOperation;

import java.util.List;

public interface IRecommendationService extends IService<UserOperation> {

    /**
     * 笔记推荐，ItemCF
     *
     * @param userId 用户 ID
     * @param count 推荐数量
     * @return 推荐笔记 ID 列表
     */
    List<Long> recommend(Long userId, int count);

    /**
     * 获取推荐器
     * @param userOperations 用户操作列表
     */
    void computeItemCF(List<UserOperation> userOperations);
}
