package org.rednote.interaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.dto.ScrollResult;
import org.rednote.interaction.api.entity.WebFollow;
import org.rednote.interaction.api.vo.FollowVO;
import org.rednote.interaction.api.vo.TrendVO;
import org.rednote.interaction.service.IWebFollowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "关注管理", description = "关注管理相关接口")
@RequestMapping("/web/follow")
@RestController
@RequiredArgsConstructor
public class WebFollowController {

    private final IWebFollowService followService;

    @Operation(summary = "获取关注用户的所有动态", description = "获取关注用户的所有动态")
    @GetMapping("getFollowTrend")
    public Result<?> getFollowTrend(@Parameter(description = "上一次的查询的最小时间戳") long lastTime,
                                    @Parameter(description = "上一次的查询中最小时间戳的个数")
                                    @RequestParam(defaultValue = "0") int offset,
                                    @Parameter(description = "查询的数量") int count) {
        ScrollResult<TrendVO> trendVOScrollResult = followService.getFollowTrend(lastTime, offset, count);
        return Result.ok(trendVOScrollResult);
    }

    @Operation(summary = "关注用户", description = "关注用户")
    @GetMapping("followById")
    public Result<?> followById(@Parameter(description = "关注用户 ID") Long followId) {
        followService.followById(followId);
        return Result.ok();
    }

    @Operation(summary = "当前用户是否关注", description = "当前用户是否关注")
    @GetMapping("isFollow")
    public Result<Boolean> isFollow(@Parameter(description = "关注用户 ID") Long followId) {
        boolean flag = followService.isFollow(followId);
        return Result.ok(flag);
    }

    @Operation(summary = "获取当前用户的最新关注信息", description = "获取当前用户的最新关注信息")
    @GetMapping("getFollowInfo/{currentPage}/{pageSize}")
    public Result<?> getFollowInfo(@Parameter(description = "当前页码") @PathVariable long currentPage,
                                   @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<FollowVO> pageInfo = followService.getFollowInfo(currentPage, pageSize);
        return Result.ok(pageInfo);
    }

    /**
     * 以下用于远程调用
     */

    @Operation(hidden = true)
    @GetMapping("getFollowByFid")
    public List<WebFollow> getFollowByFid(Long fid) {
        return followService.lambdaQuery().eq(WebFollow::getFid, fid).list();
    }
}
