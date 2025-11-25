package org.rednote.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.dto.Result;
import org.rednote.domain.entity.WebTag;
import org.rednote.domain.vo.TagVO;
import org.rednote.service.IWebTagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "标签管理", description = "标签管理相关接口")
@RequestMapping("/web/tag")
@RestController
@RequiredArgsConstructor
public class WebTagController {

    private final IWebTagService tagService;

    @Operation(summary = "获取热门标签", description = "获取热门标签")
    @GetMapping("getHotTagList/{currentPage}/{pageSize}")
    public Result<?> getHotTagList(@Parameter(description = "当前页") @PathVariable long currentPage,
                                   @Parameter(description = "分页数") @PathVariable long pageSize) {
        Page<TagVO> voList = tagService.getHotTagList(currentPage, pageSize);
        return Result.ok(voList);
    }

    @Operation(summary = "根据关键词获取标签", description = "根据关键词获取标签")
    @GetMapping("getTagByKeyword/{currentPage}/{pageSize}")
    public Result<?> getTagByKeyword(@Parameter(description = "当前页") @PathVariable long currentPage,
                                     @Parameter(description = "分页数") @PathVariable long pageSize,
                                     @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        Page<TagVO> page = tagService.getTagByKeyword(currentPage, pageSize, keyword);
        return Result.ok(page);
    }

    @Operation(summary = "获取当前标签信息", description = "获取当前标签信息")
    @GetMapping("getTagById/{tagId}")
    public Result<?> getTagById(@Parameter(description = "标签 ID") @PathVariable String tagId) {
        WebTag tag = tagService.getById(tagId);
        return Result.ok(BeanUtil.copyProperties(tag, TagVO.class));
    }
}
