package org.rednote.search.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.note.api.entity.WebNavbar;
import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebTag;
import org.rednote.note.api.entity.WebTagNoteRelation;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "note-service")
public interface NoteServiceFeign {

    @GetMapping("/web/note/getById")
    WebNote getNoteById(@RequestParam("noteId") Long noteId);

    @GetMapping("/web/note/getByIds")
    List<WebNote> getNoteByIds(@RequestParam("noteIds") List<Long> noteIds);

    @GetMapping("/web/tag/getTagByIds")
    List<WebTag> getTagByIds(@RequestParam("tagIds") List<Long> tagIds);

    @GetMapping("/web/tag/getTagNoteRelationByNid")
    List<WebTagNoteRelation> getTagNoteRelationByNid(@RequestParam("nid") Long nid);

    @PostMapping("/web/note/selectNotePageWithCondition")
    Page<WebNote> selectNotePageWithCondition(
            @RequestParam("currentPage") Long currentPage,
            @RequestParam("pageSize") Long pageSize,
            @RequestBody SearchNoteDTO searchNoteDTO);

    @GetMapping("/web/note/selectNotePage")
    Page<WebNote> selectNotePage(
            @RequestParam("currentPage") Long currentPage,
            @RequestParam("pageSize") Long pageSize);

    @GetMapping("/web/category/selectCategoryList")
    List<WebNavbar> selectCategoryListByKeyword(@RequestParam("keyword") String keyword);

    @GetMapping("/web/category/getCategoryById")
    WebNavbar getCategoryById(@RequestParam("id") Long id);
}
