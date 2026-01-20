package org.rednote.recommendation.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.common.exception.RedNoteException;
import org.rednote.recommendation.api.entity.UserOperation;
import org.rednote.recommendation.api.enums.UserOperationEnum;
import org.rednote.recommendation.feign.InteractionServiceFeign;
import org.rednote.recommendation.mapper.UserOperationMapper;
import org.rednote.recommendation.service.IRecommendationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务
 * 数据同步与预计算
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendScheduledTasks {

    private final InteractionServiceFeign interactionServiceFeign;
    private final UserOperationMapper userOperationMapper;
    private final IRecommendationService recommendationService;

    /**
     * 数据同步任务
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨 2 点执行
    @Transactional(rollbackFor = Exception.class)
    public void syncUserOperations() {
        log.info("开始同步用户操作数据...");
        // 获取前一天的数据
        // 收藏、点赞笔记
        List<UserOperation> userOperationList1 =
                interactionServiceFeign.getLikeOrFavoriteByTime(LocalDateTime.now().minusDays(1))
                        .stream()
                        .filter(likeOrFavorite -> likeOrFavorite.getType() == 1 || likeOrFavorite.getType() == 2)
                        .map(likeOrFavorite -> {
                                    UserOperation userOperation = new UserOperation();
                                    userOperation.setUid(likeOrFavorite.getUid());
                                    userOperation.setNid(likeOrFavorite.getLikeOrFavoriteId());
                                    userOperation.setOperation(
                                            likeOrFavorite.getType() == 1 ?
                                                    UserOperationEnum.LIKE :
                                                    UserOperationEnum.FAVORITE
                                    );
                                    return userOperation;
                                }
                        )
                        .toList();
        // 评论
        List<UserOperation> userOperationList2 =
                interactionServiceFeign.getCommentByTime(LocalDateTime.now().minusDays(1))
                        .stream()
                        .distinct()
                        .map(comment -> {
                            UserOperation userOperation = new UserOperation();
                            userOperation.setUid(comment.getUid());
                            userOperation.setNid(comment.getNid());
                            userOperation.setOperation(UserOperationEnum.COMMENT);
                            return userOperation;
                        })
                        .toList();
        log.info("同步到数据库中...");
        userOperationMapper.insert(userOperationList1);
        userOperationMapper.insert(userOperationList2);
        log.info("同步完成。");
    }

    /**
     * 预计算任务
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨 3 点执行
    public void preCalculate() {
        log.info("开始预计算...");
        try {
            // 获取过去 30 天的操作数据
            List<UserOperation> userOperations = userOperationMapper.selectList(
                    new LambdaQueryWrapper<UserOperation>()
                            .ge(UserOperation::getCreateTime, LocalDateTime.now().minusDays(30))
            );
            // 计算 ItemCF
            recommendationService.computeItemCF(userOperations);
        } catch (Exception e) {
            log.error("ItemCF 预计算失败：", e);
            throw new RedNoteException("ItemCF 预计算失败");
        }
        log.info("预计算完成。");
    }
}
