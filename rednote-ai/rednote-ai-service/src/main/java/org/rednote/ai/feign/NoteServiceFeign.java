package org.rednote.ai.feign;

import org.rednote.note.api.entity.WebNote;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "note-service")
public interface NoteServiceFeign {

    @GetMapping("/web/note/getById")
    WebNote getNoteById(@RequestParam("noteId") Long noteId);
}
