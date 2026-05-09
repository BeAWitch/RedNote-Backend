package org.rednote.ai.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.note.api.entity.WebNote;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "note-service")
public interface NoteServiceFeign {

    @GetMapping("/web/note/getById")
    WebNote getNoteById(@RequestParam("noteId") Long noteId);

    /** 全量分页查询笔记，仅内部使用（返回 MyBatis-Plus Page） */
    @GetMapping("/web/note/selectNotePage")
    Page<WebNote> selectNotePage(@RequestParam("currentPage") Long currentPage,
                                 @RequestParam("pageSize") Long pageSize);
}
