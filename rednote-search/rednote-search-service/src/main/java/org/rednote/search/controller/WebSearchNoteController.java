package org.rednote.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.note.api.entity.WebNavbar;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.vo.NoteSearchVO;
import org.rednote.search.service.IWebSearchNoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "笔记搜索", description = "笔记搜索相关接口")
@RestController
@RequestMapping("/web/search/note")
@RequiredArgsConstructor
public class WebSearchNoteController {

    private final IWebSearchNoteService searchNoteService;

    @PostMapping("getNoteByDTO/{currentPage}/{pageSize}")
    @Operation(summary = "条件搜索笔记", description = "根据条件分页搜索笔记")
    public Result<Page<NoteSearchVO>> getNoteByDTO(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize,
            @Parameter(description = "搜索条件") @RequestBody SearchNoteDTO searchNoteDTO) {
        Page<NoteSearchVO> page = searchNoteService.getNoteByDTO(currentPage, pageSize, searchNoteDTO);
        return Result.ok(page);
    }

    @PostMapping("getCategoryAgg")
    @Operation(summary = "获取分类聚合", description = "根据搜索条件获取分类聚合结果")
    public Result<List<WebNavbar>> getCategoryAgg(
            @Parameter(description = "搜索条件") @RequestBody SearchNoteDTO searchNoteDTO) {
        List<WebNavbar> categoryList = searchNoteService.getCategoryAgg(searchNoteDTO);
        return Result.ok(categoryList);
    }

    @GetMapping("getRecommendNote/{currentPage}/{pageSize}")
    @Operation(summary = "获取推荐笔记", description = "分页获取推荐笔记列表")
    public Result<Page<NoteSearchVO>> getRecommendNote(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<NoteSearchVO> page = searchNoteService.getRecommendNote(currentPage, pageSize);
        return Result.ok(page);
    }

    @GetMapping("getHotNote/{currentPage}/{pageSize}")
    @Operation(summary = "获取热门笔记", description = "分页获取热门笔记列表")
    public Result<Page<NoteSearchVO>> getHotNote(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<NoteSearchVO> page = searchNoteService.getHotNote(currentPage, pageSize);
        return Result.ok(page);
    }
}
