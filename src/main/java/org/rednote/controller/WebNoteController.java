package org.rednote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.rednote.domain.dto.Result;
import org.rednote.domain.vo.NoteVO;
import org.rednote.validator.myVaildator.noLogin.NoLoginIntercept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/web/note")
@RestController
@Tag(name = "笔记管理", description = "笔记相关接口")
public class WebNoteController {

    @Autowired
    private IWebNoteService noteService;

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    @GetMapping("getNoteById")
    @NoLoginIntercept
    @Operation(summary = "根据ID获取笔记", description = "根据笔记ID获取笔记详情")
    public Result<NoteVO> getNoteById(@Parameter(description = "笔记ID") String noteId) {
        NoteVO noteVo = noteService.getNoteById(noteId);
        return Result.ok(noteVo);
    }

    /**
     * 新增笔记
     *
     * @param noteData 笔记对象
     * @param files    图片文件
     */
    @PostMapping("saveNoteByDTO")
    @Operation(summary = "新增笔记", description = "创建新的笔记")
    public Result<String> saveNoteByDTO(
            @Parameter(description = "笔记数据JSON字符串") @RequestParam("noteData") String noteData,
            @Parameter(description = "上传的图片文件") @RequestParam("uploadFiles") MultipartFile[] files) {
        noteService.saveNoteByDTO(noteData, files);
        return Result.ok("创建成功");
    }

    /**
     * 删除笔记
     *
     * @param noteIds 笔记ID集合
     */
    @PostMapping("deleteNoteByIds")
    @Operation(summary = "删除笔记", description = "批量删除笔记")
    public Result<String> deleteNoteByIds(
            @Parameter(description = "笔记ID集合") @RequestBody List<String> noteIds) {
        noteService.deleteNoteByIds(noteIds);
        return Result.ok("删除成功");
    }

    /**
     * 更新笔记
     *
     * @param noteData 笔记对象
     * @param files    图片文件
     */
    @PostMapping("updateNoteByDTO")
    @Operation(summary = "更新笔记", description = "更新已存在的笔记")
    public Result<String> updateNoteByDTO(
            @Parameter(description = "笔记数据JSON字符串") @RequestParam("noteData") String noteData,
            @Parameter(description = "上传的图片文件") @RequestParam("uploadFiles") MultipartFile[] files) {
        noteService.updateNoteByDTO(noteData, files);
        return Result.ok("更新成功");
    }

    /**
     * 获取热门笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getHotPage/{currentPage}/{pageSize}")
    @Operation(summary = "获取热门笔记", description = "分页获取热门笔记列表")
    public Result<Page<NoteVO>> getHotPage(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<NoteVO> pageInfo = noteService.getHotPage(currentPage, pageSize);
        return Result.ok(pageInfo);
    }

    /**
     * 置顶笔记
     *
     * @param noteId 笔记ID
     */
    @GetMapping("pinnedNote")
    @Operation(summary = "置顶笔记", description = "置顶或取消置顶笔记")
    public Result<Boolean> pinnedNote(@Parameter(description = "笔记ID") String noteId) {
        boolean flag = noteService.pinnedNote(noteId);
        return Result.ok(flag);
    }
}
