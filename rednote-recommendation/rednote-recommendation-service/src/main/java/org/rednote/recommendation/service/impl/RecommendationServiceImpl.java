package org.rednote.recommendation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.exception.RedNoteException;
import org.rednote.common.utils.RedisUtil;
import org.rednote.recommendation.api.constant.OperationScoreConstants;
import org.rednote.recommendation.api.entity.UserOperation;
import org.rednote.recommendation.mapper.UserOperationMapper;
import org.rednote.recommendation.service.IRecommendationService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 * 使用的推荐算法：ItemCF
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl extends ServiceImpl<UserOperationMapper, UserOperation> implements IRecommendationService {

    private final RedisUtil redisUtil;

    /**
     * 推荐，ItemCF
     */
    @Override
    public List<Long> recommend(Long userId, int count) {
        // 获取用户过去 30 天的操作数据
        List<UserOperation> userOperations = this.list(
                new LambdaQueryWrapper<>(UserOperation.class)
                        .eq(UserOperation::getUid, userId)
                        .ge(UserOperation::getCreateTime, LocalDateTime.now().minusDays(30))
        );
        // 检查数据是否足够
        if (userOperations.isEmpty()) {
            log.warn("用户操作为空，无法进行推荐。");
            return Collections.emptyList();
        }

        // 提取用户最近操作过的 item
        List<Long> recentItems = userOperations.stream()
                .map(UserOperation::getNid)
                .distinct()
                .limit(20) // 只取 20 个
                .toList();

        // ItemCF 分数容器
        Map<Long, Double> candidateScores = new HashMap<>();
        // 从 Redis 查 ItemCF 相似结果
        for (Long itemId : recentItems) {
            String key = RedisConstants.ITEM_CF_KEY + itemId;
            // Top50
            Set<ZSetOperations.TypedTuple<Object>> similarItems = redisUtil.zRangeWithScores(key, 0, 50);
            if (similarItems == null) {
                continue;
            }
            for (ZSetOperations.TypedTuple<Object> sim : similarItems) {
                Long simItemId = Long.valueOf((String) sim.getValue());
                Double score = sim.getScore();
                // 累加相似度
                candidateScores.merge(simItemId, score, Double::sum);
            }
        }
        if (candidateScores.isEmpty()) {
            return Collections.emptyList();
        }

        // 去掉用户已经操作过的 item
        Set<Long> operatedItems = new HashSet<>(recentItems);
        operatedItems.forEach(candidateScores::remove);

        // TopN 排序返回
        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 计算 ItemCF
     * 缓存每个 Item 相似度最高的 100 个 Item
     */
    @Override
    public void computeItemCF(List<UserOperation> userOperations) {
        try {
            DataModel dataModel = createDataModel(userOperations);
            ItemSimilarity itemSimilarity = new UncenteredCosineSimilarity(dataModel);
            ItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, itemSimilarity);
            LongPrimitiveIterator itemIter = dataModel.getItemIDs();

            while (itemIter.hasNext()) {
                long itemId = itemIter.nextLong();
                List<RecommendedItem> similarItems = recommender.mostSimilarItems(itemId, 100);
                saveItemCFToRedis(itemId, similarItems);
            }
        } catch (TasteException e) {
            log.error("ItemCF 计算失败。");
            throw new RedNoteException("ItemCF 计算失败");
        }
    }

    /**
     * 保存 ItemCF 结果到 Redis
     */
    private void saveItemCFToRedis(long itemId, List<RecommendedItem> similarItems) {
        String key = RedisConstants.ITEM_CF_KEY + itemId;
        // 先清空旧数据
        redisUtil.delete(key);
        for (RecommendedItem item : similarItems) {
            redisUtil.zAdd(
                    key,
                    String.valueOf(item.getItemID()),
                    item.getValue()
            );
        }
        redisUtil.expire(key, RedisConstants.ITEM_CF_TTL, RedisConstants.ITEM_CF_TTL_UNIT);
    }

    /**
     * 创建数据模型
     */
    private DataModel createDataModel(List<UserOperation> UserOperations) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        // 获取用户对应的所有操作
        Map<Long, List<UserOperation>> map =
                UserOperations.stream().collect(Collectors.groupingBy(UserOperation::getUid));
        Collection<List<UserOperation>> list = map.values();
        for (List<UserOperation> userPreferences : list) {
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for (int i = 0; i < userPreferences.size(); i++) {
                UserOperation userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUid(),
                        userPreference.getNid(), calculateScore(userPreference));
                array[i] = item;
            }
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }

    /**
     * 计算操作分数
     */
    private float calculateScore(UserOperation userOperation) {
        return switch (userOperation.getOperation()) {
            case LIKE -> OperationScoreConstants.LIKE;
            case FAVORITE -> OperationScoreConstants.FAVORITE;
            case COMMENT -> OperationScoreConstants.COMMENT;
        };
    }
}
