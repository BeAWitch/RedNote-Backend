package org.rednote.note.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebUserNoteRelation;
import org.rednote.note.api.vo.NoteVO;
import org.rednote.note.service.IWebNoteService;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.vo.NoteSearchVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public Result<NoteVO> getNoteById(@Parameter(description = "笔记ID") Long noteId) {
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
    public Result<Boolean> pinnedNote(@Parameter(description = "笔记ID") Long noteId) {
        boolean flag = noteService.pinnedNote(noteId);
        return Result.ok(flag);
    }

    @Operation(summary = "获取用户的动态", description = "获取用户的动态")
    @GetMapping("getTrendByUser/{currentPage}/{pageSize}")
    public Result<?> getTrendByUser(@Parameter(description = "当前页码") @PathVariable long currentPage,
                                    @Parameter(description = "每页大小") @PathVariable long pageSize,
                                    @Parameter(description = "用户 ID") Long userId,
                                    @Parameter(description = "类型（1：笔记，2：点赞，3：收藏）") int type) {
        Page<NoteSearchVO> pageInfo = noteService.getTrendByUser(currentPage, pageSize, userId, type);
        return Result.ok(pageInfo);
    }

    /**
     * 下方用于远程调用
     */

    @Operation(hidden = true)
    @GetMapping("getById")
    public WebNote getById(@RequestParam("noteId") Long noteId) {
        return noteService.getById(noteId);
    }

    @Operation(hidden = true)
    @GetMapping("getByIds")
    public List<WebNote> getByIds(@RequestParam("noteIds") List<Long> noteIds) {
        return noteService.listByIds(noteIds);
    }

    @Operation(hidden = true)
    @GetMapping("getByIdsOrderedByTime")
    public List<WebNote> getByIdsOrderedByTime(@RequestParam("noteIds") List<Long> noteIds) {
        return noteService.getByIdsOrderedByTime(noteIds);
    }

    @Operation(hidden = true)
    @PostMapping("updateNote")
    public Boolean updateNote(@RequestBody WebNote note) {
        return noteService.updateById(note);
    }

    @Operation(hidden = true)
    @PostMapping("selectNotePageWithCondition")
    public Page<WebNote> selectNotePageWithCondition(
            @RequestParam("currentPage") Long currentPage,
            @RequestParam("pageSize") Long pageSize,
            @RequestBody SearchNoteDTO searchNoteDTO) {
        return noteService.selectNotePageWithCondition(new Page<>(currentPage, pageSize) ,searchNoteDTO);
    }

    @Operation(hidden = true)
    @GetMapping("selectNotePage")
    public Page<WebNote> selectNotePage(
            @RequestParam("currentPage") Long currentPage,
            @RequestParam("pageSize") Long pageSize) {
        return noteService.page(new Page<>(currentPage, pageSize));
    }

    @Operation(hidden = true)
    @GetMapping("getUserNoteRelationByUserId")
    List<WebUserNoteRelation> getUserNoteRelationByUserId(@RequestParam("userId") Long userId) {
        return noteService.getUserNoteRelationByUserId(userId);
    }
}
