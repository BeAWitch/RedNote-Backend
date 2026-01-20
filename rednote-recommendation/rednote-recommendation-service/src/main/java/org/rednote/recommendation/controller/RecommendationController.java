package org.rednote.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.recommendation.schedule.RecommendScheduledTasks;
import org.rednote.recommendation.service.IRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "笔记推荐", description = "笔记推荐相关接口")
@RequestMapping("/web/recommendation")
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final IRecommendationService recommendationService;

    @Operation(summary = "获取推荐笔记", description = "获取推荐笔记")
    @GetMapping("recommendNotes")
    public List<Long> recommendNotes(
            @Parameter(description = "用户 ID") @RequestParam("uid") Long uid,
            @Parameter(description = "笔记数量") @RequestParam("count") int count) {
        return recommendationService.recommend(uid, count);
    }

    private final RecommendScheduledTasks recommendScheduledTasks;

    @Operation(summary = "预计算测试接口", description = "预计算测试接口")
    @GetMapping("preCalculate")
    public void preCalculate() {
        recommendScheduledTasks.preCalculate();
    }
}
