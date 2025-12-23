package org.rednote.interaction.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.utils.UserHolder;
import org.rednote.interaction.api.dto.LikeOrFavoriteDTO;
import org.rednote.interaction.api.dto.ScrollResult;
import org.rednote.interaction.api.dto.WSMessageDTO;
import org.rednote.interaction.api.entity.WebFollow;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.rednote.interaction.api.util.WebSocketServer;
import org.rednote.interaction.api.vo.FollowVO;
import org.rednote.interaction.api.vo.TrendVO;
import org.rednote.interaction.feign.NoteServiceFeign;
import org.rednote.interaction.feign.UserServiceFeign;
import org.rednote.interaction.mapper.WebFollowMapper;
import org.rednote.interaction.service.IWebChatService;
import org.rednote.interaction.service.IWebFollowService;
import org.rednote.interaction.service.IWebLikeOrFavoriteService;
import org.rednote.user.api.entity.WebUser;
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

    private final NoteServiceFeign noteServiceFeign;
    private final UserServiceFeign userServiceFeign;
    private final IWebLikeOrFavoriteService likeOrFavoriteService;
    private final IWebChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;
    private final WebSocketServer webSocketServer;

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
        List<TrendVO> trendVOS = noteServiceFeign.getByIdsOrderedByTime(nids)
                .stream()
                //.filter(note -> note.getAuditStatus() == 1) // TODO 审核验证
                .map(note -> {
                    TrendVO trendVO = new TrendVO();
                    Long uid = note.getUid();
                    WebUser user = userServiceFeign.getUserById(uid).getData();
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
        WebUser currentUser = userServiceFeign.getUserById(userId).getData();
        WebUser followedUser = userServiceFeign.getUserById(followId).getData();
        if (isFollow(followId)) {
            currentUser.setFollowCount(currentUser.getFollowCount() - 1);
            followedUser.setFollowerCount(followedUser.getFollowerCount() - 1);
            this.remove(new QueryWrapper<WebFollow>().eq("uid", userId).eq("fid", followId));
            // 更新 Redis
            chatService.decreaseUncheckedMessageCount(UncheckedMessageEnum.FOLLOW, followId, 1L);
            // Websocket 消息推送
            webSocketServer.sendMessage(new WSMessageDTO()
                    .setAcceptUid(followId)
                    .setType(UncheckedMessageEnum.FOLLOW)
                    .setContent(-1L)
            );
        } else {
            currentUser.setFollowCount(currentUser.getFollowCount() + 1);
            followedUser.setFollowerCount(followedUser.getFollowerCount() + 1);
            this.save(follow);
            // 更新 Redis
            chatService.increaseUncheckedMessageCount(UncheckedMessageEnum.FOLLOW, followId, 1L);
            // Websocket 消息推送
            webSocketServer.sendMessage(new WSMessageDTO()
                    .setAcceptUid(followId)
                    .setType(UncheckedMessageEnum.COMMENT)
                    .setContent(1L)
            );
        }
        userServiceFeign.updateUser(currentUser);
        userServiceFeign.updateUser(followedUser);
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

        List<Long> uids = followList.stream().map(WebFollow::getUid).toList();
        Map<Long, WebUser> userMap = userServiceFeign.getUserByIds(uids)
                .stream().collect(Collectors.toMap(WebUser::getId, user -> user));

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
