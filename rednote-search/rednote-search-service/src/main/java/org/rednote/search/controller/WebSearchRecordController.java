package org.rednote.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.search.service.IWebSearchRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "搜索记录管理", description = "搜索记录相关接口")
@RequestMapping("/web/search/record")
@RestController
@RequiredArgsConstructor
public class WebSearchRecordController {

    private final IWebSearchRecordService searchRecordService;

    @Operation(summary = "获取搜索记录", description = "获取搜索记录")
    @GetMapping("getRecordByKeyWord/{keyword}")
    public Result<?> getRecordByKeyWord(@Parameter(description = "关键词") @PathVariable("keyword") String keyword) {
        return Result.ok(searchRecordService.getRecordByKeyWord(keyword));
    }

    @Operation(summary = "热门关键词", description = "热门关键词")
    @GetMapping("getHotRecord/{count}")
    public Result<?> getHotRecord(@Parameter(description = "关键词个数") @PathVariable("count") int count) {
        return Result.ok(searchRecordService.getHotRecord(count));
    }

    @Operation(summary = "增加搜索记录", description = "增加搜索记录")
    @PostMapping("addRecord")
    public Result<?> addRecord(@Parameter(description = "搜索记录") @RequestParam String keyword) {
        searchRecordService.addRecord(keyword);
        return Result.ok();
    }
}
