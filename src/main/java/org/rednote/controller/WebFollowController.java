/*
package org.rednote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.dto.Result;
import org.rednote.domain.vo.FollowVO;
import org.rednote.domain.vo.TrendVO;
import org.rednote.service.IWebFollowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

*/
/**
 * 关注
 *//*

@Tag(name = "笔记搜索", description = "笔记搜索相关接口")
@RequestMapping("/web/follower")
@RestController
@RequiredArgsConstructor
public class WebFollowController {

    private final IWebFollowService followService;


    */
/**
     * 获取关注用户的所有动态
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     *//*

    @GetMapping("getFollowTrend/{currentPage}/{pageSize}")
    public Result<?> getFollowTrend(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<TrendVO> pageInfo = followService.getFollowTrend(currentPage, pageSize);
        return Result.ok(pageInfo);
    }

    */
/**
     * 获取当前用户所有的关注和粉丝
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param type        类型
     *//*

    @GetMapping("getFriend/{currentPage}/{pageSize}")
    public Result<?> getFriend(@PathVariable long currentPage, @PathVariable long pageSize, String uid, Integer type) {
        Page<FollowVO> pageInfo = followService.getFriend(currentPage, pageSize, uid, type);
        return Result.ok(pageInfo);
    }

    */
/**
     * 关注用户
     *
     * @param followerId 关注用户ID
     *//*

    @GetMapping("followById")
    public Result<?> followById(String followerId) {
        followService.followById(followerId);
        return Result.ok();
    }

    */
/**
     * 当前用户是否关注
     *
     * @param followerId 关注的用户ID
     *//*

    @GetMapping("isFollow")
    public Result<?> isFollow(String followerId) {
        boolean flag = followService.isFollow(followerId);
        return Result.ok(flag);
    }

    */
/**
     * 获取当前用户的最新关注信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     *//*

    @GetMapping("getNoticeFollower/{currentPage}/{pageSize}")
    public Result<?> getNoticeFollower(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<FollowVO> pageInfo = followService.getNoticeFollower(currentPage, pageSize);
        return Result.ok(pageInfo);
    }
}
*/
