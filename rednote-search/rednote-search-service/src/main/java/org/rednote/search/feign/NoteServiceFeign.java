package org.rednote.search.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.note.api.entity.WebNavbar;
import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebTag;
import org.rednote.note.api.entity.WebTagNoteRelation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "note-service")
public interface NoteServiceFeign {

    @GetMapping("/web/tag/getTagByIds")
    List<WebTag> getTagByIds(List<Long> tagIds);

    @GetMapping("/web/tag/getTagNoteRelationByNid")
    List<WebTagNoteRelation> getTagNoteRelationByNid(Long nid);

    @GetMapping("/web/note/selectNotePage")
    Page<WebNote> selectNotePage(
            @RequestParam("page") Page<WebNote> page,
            @RequestParam("queryWrapper") LambdaQueryWrapper<WebNote> queryWrapper);

    @GetMapping("/web/category/selectCategoryList")
    List<WebNavbar> selectCategoryList(LambdaQueryWrapper<WebNavbar> queryWrapper);

    @GetMapping("/web/category/getCategoryById")
    WebNavbar getCategoryById(Long id);
}
