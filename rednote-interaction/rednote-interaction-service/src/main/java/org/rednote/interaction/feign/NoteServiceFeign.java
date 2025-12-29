package org.rednote.interaction.feign;

import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebUserNoteRelation;
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

    @GetMapping("/web/note/getByIdsOrderedByTime")
    List<WebNote> getByIdsOrderedByTime(@RequestParam("noteIds") List<Long> noteIds);

    @PostMapping("/web/note/updateNote")
    Boolean updateNote(@RequestBody WebNote note);

    @GetMapping("/web/note/getUserNoteRelationByUserId")
    List<WebUserNoteRelation> getUserNoteRelationByUserId(@RequestParam("userId") Long userId);
}
