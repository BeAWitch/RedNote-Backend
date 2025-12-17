package org.rednote.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.constant.RedisConstants;
import org.rednote.domain.dto.LikeOrFavoriteDTO;
import org.rednote.domain.dto.ScrollResult;
import org.rednote.domain.entity.WebFollow;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.vo.FollowVO;
import org.rednote.domain.vo.TrendVO;
import org.rednote.enums.UncheckedMessageEnum;
import org.rednote.mapper.WebFollowMapper;
import org.rednote.mapper.WebNoteMapper;
import org.rednote.mapper.WebUserMapper;
import org.rednote.service.IWebChatService;
import org.rednote.service.IWebFollowService;
import org.rednote.service.IWebLikeOrFavoriteService;
import org.rednote.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebFollowServiceImpl extends ServiceImpl<WebFollowMapper, WebFollow> implements IWebFollowService {

    private final WebNoteMapper noteMapper;
    private final WebUserMapper userMapper;
    private final IWebLikeOrFavoriteService likeOrFavoriteService;
    private final IWebChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取关注用户的所有动态
     *
     * @param lastTime 上一次的查询的最小时间戳
     * @param offset   上一次的查询中最小时间戳的个数
     * @param count    查询的数量
     */
    @Override
    public ScrollResult<TrendVO> getFollowTrend(long lastTime, int offset, int count) {
        Long currentUid = UserHolder.getUserId();

        // 查询 Redis
        // 查询当前用户的收件箱
        String key = RedisConstants.TREND_KEY + currentUid;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, lastTime, offset, count);
        // 非空判断
        if (CollUtil.isEmpty(typedTuples)) {
            return null;
        }
        // 解析数据
        List<Long> nids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int minCount = 1; // 重复的个数，下一次请求中的偏移量
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            // 获取 ID
            nids.add(Long.valueOf(tuple.getValue()));
            // 获取分数（时间戳）
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                minCount++;
            } else {
                minTime = time;
                minCount = 1;
            }
        }
        minCount = minTime == lastTime ? minCount : minCount + offset;

        // 查询笔记并填充 VO
        List<TrendVO> trendVOS = noteMapper.selectBatchIds(nids).stream()
                //.filter(note -> note.getAuditStatus() == 1) // TODO 审核验证
                .map(note -> {
                    TrendVO trendVO = new TrendVO();
                    Long uid = note.getUid();
                    WebUser user = userMapper.selectById(uid);
                    trendVO.setUid(user.getId())
                            .setUsername(user.getUsername())
                            .setAvatar(user.getAvatar())
                            .setNid(note.getId())
                            .setTime(note.getUpdateTime().getTime())
                            .setContent(note.getTitle())
                            .setCommentCount(note.getCommentCount())
                            .setLikeCount(note.getLikeCount())
                            .setIsLoading(false);
                    LikeOrFavoriteDTO likeOrFavoriteDTO = new LikeOrFavoriteDTO();
                    likeOrFavoriteDTO.setType(1);
                    likeOrFavoriteDTO.setLikeOrFavoriteId(note.getId());
                    trendVO.setIsLike(likeOrFavoriteService.isLikeOrFavorite(likeOrFavoriteDTO));
                    String urls = note.getUrls();
                    List<String> imgList = JSONUtil.toList(urls, String.class);
                    trendVO.setImgUrls(imgList);

                    return trendVO;
                })
                .toList();

        // 填充返回结果
        ScrollResult<TrendVO> trendVOScrollResult = new ScrollResult<>();
        trendVOScrollResult.setList(trendVOS);
        trendVOScrollResult.setMinTime(minTime);
        trendVOScrollResult.setOffset(minCount);

        return trendVOScrollResult;
    }

    /**
     * 关注用户
     *
     * @param followId 关注用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void followById(Long followId) {
        WebFollow follow = new WebFollow();
        Long userId = UserHolder.getUserId();
        follow.setUid(userId);
        follow.setFid(followId);
        // 得到当前用户
        WebUser currentUser = userMapper.selectById(userId);
        WebUser followedUser = userMapper.selectById(followId);
        if (isFollow(followId)) {
            currentUser.setFollowCount(currentUser.getFollowCount() - 1);
            followedUser.setFollowerCount(followedUser.getFollowerCount() - 1);
            this.remove(new QueryWrapper<WebFollow>().eq("uid", userId).eq("fid", followId));
            // 消息推送
            chatService.decreaseUncheckedMessageCount(UncheckedMessageEnum.FOLLOW_COUNT, followId, 1);
        } else {
            currentUser.setFollowCount(currentUser.getFollowCount() + 1);
            followedUser.setFollowerCount(followedUser.getFollowerCount() + 1);
            this.save(follow);
            // 消息推送
            chatService.increaseUncheckedMessageCount(UncheckedMessageEnum.FOLLOW_COUNT, followId, 1);
        }
        userMapper.updateById(currentUser);
        userMapper.updateById(followedUser);
    }

    /**
     * 获取当前用户的最新关注信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<FollowVO> getFollowInfo(long currentPage, long pageSize) {
        Page<FollowVO> result = new Page<>();
        Long userId = UserHolder.getUserId();

        Page<WebFollow> followPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebFollow>()
                        .eq("fid", userId)
                        .ne("uid", userId)
                        .orderByDesc("create_time"));
        List<WebFollow> followList = followPage.getRecords();
        long total = followPage.getTotal();

        Set<Long> uids = followList.stream().map(WebFollow::getUid).collect(Collectors.toSet());
        Map<Long, WebUser> userMap = userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(WebUser::getId
                , user -> user));

        // 得到当前用户的所有关注
        List<WebFollow> followers = this.list(new QueryWrapper<WebFollow>().eq("uid", userId));
        Set<Long> followerSet = followers.stream().map(WebFollow::getFid).collect(Collectors.toSet());

        List<FollowVO> followVOList = new ArrayList<>();
        followList.forEach(item -> {
            FollowVO followVo = new FollowVO();
            WebUser user = userMap.get(item.getUid());
            followVo.setUid(user.getId())
                    .setUsername(user.getUsername())
                    .setAvatar(user.getAvatar())
                    .setTime(item.getCreateTime().getTime())
                    .setIsFollow(followerSet.contains(item.getUid()));
            followVOList.add(followVo);
        });

        result.setRecords(followVOList);
        result.setTotal(total);
        return result;
    }

    /**
     * 当前用户是否关注
     *
     * @param followerId 关注的用户ID
     */
    @Override
    public boolean isFollow(Long followerId) {
        Long userId = UserHolder.getUserId();
        long count = this.count(new QueryWrapper<WebFollow>().eq("uid", userId).eq("fid", followerId));
        return count > 0;
    }
}
