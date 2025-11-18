package org.rednote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.rednote.domain.dto.Result;
import org.rednote.domain.dto.SearchNoteDTO;
import org.rednote.domain.entity.WebNavbar;
import org.rednote.domain.vo.NoteSearchVO;
import org.rednote.validator.myVaildator.noLogin.NoLoginIntercept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/web/search/note")
@Tag(name = "笔记搜索", description = "笔记搜索相关接口")
public class WebSearchNoteController {

    @Autowired
    private IWebSearchNoteService noteService;

    /**
     * 搜索对应的笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param searchNoteDTO   笔记搜索条件
     */
    @NoLoginIntercept
    @PostMapping("getNoteByDTO/{currentPage}/{pageSize}")
    @Operation(summary = "条件搜索笔记", description = "根据条件分页搜索笔记")
    public Result<Page<NoteSearchVO>> getNoteByDTO(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize,
            @Parameter(description = "搜索条件") @RequestBody SearchNoteDTO searchNoteDTO) {
        Page<NoteSearchVO> page = noteService.getNoteByDTO(currentPage, pageSize, searchNoteDTO);
        return Result.ok(page);
    }

    /**
     * 搜索对应的笔记分类聚合
     *
     * @param searchNoteDTO 笔记搜索条件
     */
    @NoLoginIntercept
    @PostMapping("getCategoryAgg")
    @Operation(summary = "获取分类聚合", description = "根据搜索条件获取分类聚合结果")
    public Result<List<WebNavbar>> getCategoryAgg(
            @Parameter(description = "搜索条件") @RequestBody SearchNoteDTO searchNoteDTO) {
        List<WebNavbar> categoryList = noteService.getCategoryAgg(searchNoteDTO);
        return Result.ok(categoryList);
    }

    /**
     * 分页查询推荐笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @NoLoginIntercept
    @GetMapping("getRecommendNote/{currentPage}/{pageSize}")
    @Operation(summary = "获取推荐笔记", description = "分页获取推荐笔记列表")
    public Result<Page<NoteSearchVO>> getRecommendNote(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<NoteSearchVO> page = noteService.getRecommendNote(currentPage, pageSize);
        return Result.ok(page);
    }

    /**
     * 获取热门笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getHotNote/{currentPage}/{pageSize}")
    @Operation(summary = "获取热门笔记", description = "分页获取热门笔记列表")
    public Result<Page<NoteSearchVO>> getHotNote(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<NoteSearchVO> page = noteService.getHotNote(currentPage, pageSize);
        return Result.ok(page);
    }

    /**
     * 增加笔记
     *
     * @param noteSearchVo 笔记
     */
    @PostMapping("addNote")
    @Operation(summary = "添加笔记", description = "向搜索索引中添加笔记")
    public Result<String> addNote(@Parameter(description = "笔记数据") @RequestBody NoteSearchVO noteSearchVo) {
        noteService.addNote(noteSearchVo);
        return Result.ok("添加成功");
    }

    /**
     * 修改笔记
     *
     * @param noteSearchVo 笔记
     */
    @PostMapping("updateNote")
    @Operation(summary = "更新笔记", description = "更新搜索索引中的笔记")
    public Result<String> updateNote(@Parameter(description = "笔记数据") @RequestBody NoteSearchVO noteSearchVo) {
        noteService.updateNote(noteSearchVo);
        return Result.ok("更新成功");
    }

    /**
     * 删除es中的笔记
     *
     * @param noteId 笔记ID
     */
    @DeleteMapping("deleteNote/{noteId}")
    @Operation(summary = "删除笔记", description = "从搜索索引中删除笔记")
    public Result<String> deleteNote(@Parameter(description = "笔记ID") @PathVariable String noteId) {
        noteService.deleteNote(noteId);
        return Result.ok("删除成功");
    }

    /**
     * 批量增加笔记
     */
    @PostMapping("addNoteBulkData")
    @NoLoginIntercept
    @Operation(summary = "批量添加笔记", description = "批量向搜索索引中添加笔记数据")
    public Result<String> addNoteBulkData() {
        noteService.addNoteBulkData();
        return Result.ok("批量添加成功");
    }

    /**
     * 清空笔记
     */
    @DeleteMapping("delNoteBulkData")
    @NoLoginIntercept
    @Operation(summary = "清空笔记数据", description = "清空搜索索引中的所有笔记数据")
    public Result<String> delNoteBulkData() {
        noteService.delNoteBulkData();
        return Result.ok("清空成功");
    }

    /**
     * 重置索引数据
     */
    @PostMapping("refreshNoteData")
    @NoLoginIntercept
    @Operation(summary = "刷新笔记数据", description = "重新构建搜索索引数据")
    public Result<String> refreshNoteData() {
        noteService.refreshNoteData();
        return Result.ok("刷新成功");
    }
}
