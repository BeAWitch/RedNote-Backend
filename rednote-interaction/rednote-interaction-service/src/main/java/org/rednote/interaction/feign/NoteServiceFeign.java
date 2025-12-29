package org.rednote.interaction.feign;

import org.rednote.note.api.entity.WebAlbum;
import org.rednote.note.api.entity.WebAlbumNoteRelation;
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

    @GetMapping("/web/album/getAlbumById")
    WebAlbum getAlbumById(@RequestParam("albumId") Long albumId);

    @GetMapping("/web/album/getAlbumByIds")
    List<WebAlbum> getAlbumByIds(@RequestParam("albumIds") List<Long> albumIds);

    @GetMapping("/web/album/getAlbumByIdAndType")
    WebAlbum getAlbumByIdAndType(@RequestParam("albumId") Long albumId, @RequestParam("type") Integer type);

    @PostMapping("/web/album/addAlbum")
    Boolean addAlbum(@RequestBody WebAlbum album);

    @PostMapping("/web/album/updateAlbum")
    Boolean updateAlbum(@RequestBody WebAlbum album);

    @PostMapping("/web/album/addAlbumNoteRelation")
    Boolean addAlbumNoteRelation(@RequestBody WebAlbumNoteRelation albumNoteRelation);

    @PostMapping("/web/album/updateAlbumNoteRelation")
    Boolean updateAlbumNoteRelation(@RequestBody WebAlbumNoteRelation albumNoteRelation);

    @PostMapping("/web/album/deleteAlbumNoteRelation")
    Boolean deleteAlbumNoteRelationByAidAndNid(@RequestParam("aid") Long aid, @RequestParam("nid") Long nid);

    @GetMapping("/web/album/getAlbumNoteRelationByNid")
    List<WebAlbumNoteRelation> getAlbumNoteRelationByNid(@RequestParam("nid") Long nid);
}
