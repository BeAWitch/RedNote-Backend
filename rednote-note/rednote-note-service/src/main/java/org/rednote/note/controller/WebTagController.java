package org.rednote.note.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.note.api.entity.WebTag;
import org.rednote.note.api.entity.WebTagNoteRelation;
import org.rednote.note.api.vo.TagVO;
import org.rednote.note.service.IWebTagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @GetMapping("getTagById")
    public Result<?> getTagById(@Parameter(description = "标签 ID") @RequestParam String tagId) {
        WebTag tag = tagService.getById(tagId);
        return Result.ok(BeanUtil.copyProperties(tag, TagVO.class));
    }

    /**
     * 以下用于远程调用
     */

    @Operation(hidden = true)
    @GetMapping("getTagByIds")
    public List<WebTag> getTagByIds(@RequestParam("tagIds") List<Long> tagIds) {
        return tagService.listByIds(tagIds);
    }

    @Operation(hidden = true)
    @GetMapping("getTagNoteRelationByNid")
    public List<WebTagNoteRelation> getTagNoteRelationByNid(@RequestParam("nid") Long nid) {
        return tagService.getTagNoteRelationByNid(nid);
    }
}
