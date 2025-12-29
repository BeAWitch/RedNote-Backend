package org.rednote.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.interaction.api.dto.ScrollResult;
import org.rednote.interaction.api.entity.WebFollow;
import org.rednote.interaction.api.vo.FollowVO;
import org.rednote.interaction.api.vo.TrendVO;

/**
 * 关注
 */
public interface IWebFollowService extends IService<WebFollow> {

    /**
     * 获取关注用户的所有动态
     *
     * @param lastTime 上一次的查询的最小时间戳
     * @param offset   上一次的查询中最小时间戳的个数
     * @param count    查询的数量
     */
    ScrollResult<TrendVO> getFollowTrend(Long lastTime, Integer offset, Integer count);

    /**
     * 关注用户
     *
     * @param followerId 关注用户ID
     */
    void followById(Long followerId);

    /**
     * 当前用户是否关注
     *
     * @param followId 关注的用户ID
     */
    boolean isFollow(Long followId);

    /**
     * 获取当前用户的最新关注信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<FollowVO> getFollowInfo(long currentPage, long pageSize);
}
