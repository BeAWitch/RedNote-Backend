package org.rednote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.dto.Result;
import org.rednote.domain.vo.NoteVO;
import org.rednote.service.IWebNoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "笔记管理", description = "笔记相关接口")
@RequestMapping("/web/note")
@RestController
@RequiredArgsConstructor
public class WebNoteController {

    private final IWebNoteService noteService;


    @Operation(summary = "根据 ID 获取笔记", description = "根据笔记 ID 获取笔记详情")
    @GetMapping("getNoteById")
    public Result<NoteVO> getNoteById(@Parameter(description = "笔记ID") String noteId) {
        NoteVO noteVo = noteService.getNoteById(noteId);
        return Result.ok(noteVo);
    }

    @Operation(summary = "新增笔记", description = "创建新的笔记")
    @PostMapping("saveNoteByDTO")
    public Result<String> saveNoteByDTO(
            @Parameter(description = "笔记数据 JSON 字符串") @RequestParam("noteData") String noteData,
            @Parameter(description = "上传的图片文件") @RequestParam("uploadFiles") MultipartFile[] files) {
        noteService.saveNoteByDTO(noteData, files);
        return Result.ok("创建成功");
    }

    @Operation(summary = "删除笔记", description = "批量删除笔记")
    @PostMapping("deleteNoteByIds")
    public Result<String> deleteNoteByIds(
            @Parameter(description = "笔记ID集合") @RequestBody List<String> noteIds) {
        noteService.deleteNoteByIds(noteIds);
        return Result.ok("删除成功");
    }

    @Operation(summary = "更新笔记", description = "更新已存在的笔记")
    @PostMapping("updateNoteByDTO")
    public Result<String> updateNoteByDTO(
            @Parameter(description = "笔记数据JSON字符串") @RequestParam("noteData") String noteData,
            @Parameter(description = "上传的图片文件") @RequestParam("uploadFiles") MultipartFile[] files) {
        noteService.updateNoteByDTO(noteData, files);
        return Result.ok("更新成功");
    }

    @Operation(summary = "置顶笔记", description = "置顶或取消置顶笔记")
    @GetMapping("pinnedNote")
    public Result<Boolean> pinnedNote(@Parameter(description = "笔记ID") String noteId) {
        boolean flag = noteService.pinnedNote(noteId);
        return Result.ok(flag);
    }
}
