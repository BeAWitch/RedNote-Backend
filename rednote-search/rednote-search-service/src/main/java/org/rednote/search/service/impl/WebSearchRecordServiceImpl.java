package org.rednote.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.common.constant.RedisConstants;
import org.rednote.search.api.vo.RecordSearchVO;
import org.rednote.search.service.IWebSearchRecordService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 搜索记录
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSearchRecordServiceImpl implements IWebSearchRecordService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取搜索记录
     */
    @Override
    public List<RecordSearchVO> getRecordByKeyWord(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return new ArrayList<>();
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        // 获取所有关键词和搜索次数
        Set<ZSetOperations.TypedTuple<String>> allKeywords =
                stringRedisTemplate.opsForZSet().rangeWithScores(RedisConstants.SEARCH_RECORD_KEY, 0, -1);

        if (CollUtil.isEmpty(allKeywords)) {
            return new ArrayList<>();
        }

        // 模糊匹配
        return allKeywords.stream()
                .filter(tuple -> tuple.getValue().contains(normalizedKeyword))
                .map(tuple -> {
                    RecordSearchVO recordSearchVO = new RecordSearchVO();
                    recordSearchVO.setSearchCount(tuple.getScore().longValue());
                    recordSearchVO.setContent(tuple.getValue());
                    return recordSearchVO;
                })
                .sorted((k1, k2) -> Long.compare(k2.getSearchCount(), k1.getSearchCount())) // 按搜索次数降序
                .toList();
    }

    /**
     * 热门搜索
     */
    @Override
    public List<RecordSearchVO> getHotRecord(int count) {
        Set<String> topKeywords =
                stringRedisTemplate.opsForZSet().range(RedisConstants.SEARCH_RECORD_KEY, 0, count - 1);

        if (CollUtil.isEmpty(topKeywords)) {
            return new ArrayList<>();
        }

        return topKeywords.stream()
                .map(keyword -> {
                    RecordSearchVO recordSearchVO = new RecordSearchVO();
                    recordSearchVO.setContent(keyword);
                    return recordSearchVO;
                })
                .toList();
    }

    /**
     * 增加搜索记录
     */
    @Override
    public void addRecord(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        // 添加到 Redis，如果关键词不存在则创建，存在则增加搜索次数
        stringRedisTemplate.opsForZSet().incrementScore(RedisConstants.SEARCH_RECORD_KEY, normalizedKeyword, 1);
        // 设置过期时间（每次添加都重置过期时间）
        stringRedisTemplate.expire(RedisConstants.SEARCH_RECORD_KEY,RedisConstants.SEARCH_RECORD_TTL);
    }

    /**
     * 清空搜索记录
     */
    @Override
    public void clearAllRecord() {
        stringRedisTemplate.opsForZSet().remove(RedisConstants.SEARCH_RECORD_KEY);
    }
}
