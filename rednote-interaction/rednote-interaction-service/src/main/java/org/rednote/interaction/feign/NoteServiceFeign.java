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
    WebNote getNoteById(Long noteId);

    @GetMapping("/web/note/getByIds")
    List<WebNote> getNoteByIds(List<Long> noteIds);

    @GetMapping("/web/note/getByIdsOrderedByTime")
    List<WebNote> getByIdsOrderedByTime(List<Long> noteIds);

    @PostMapping("/web/note/updateNote")
    boolean updateNote(@RequestBody WebNote note);

    @GetMapping("/web/note/getUserNoteRelationByUserId")
    List<WebUserNoteRelation> getUserNoteRelationByUserId(Long userId);

    @GetMapping("/web/album/getAlbumById")
    WebAlbum getAlbumById(Long albumId);

    @GetMapping("/web/album/getAlbumByIds")
    List<WebAlbum> getAlbumByIds(List<Long> albumIds);

    @GetMapping("/web/album/getAlbumByIdAndType")
    WebAlbum getAlbumByIdAndType(@RequestParam("albumId") Long albumId, @RequestParam("type") Integer type);

    @PostMapping("/web/album/addAlbum")
    boolean addAlbum(@RequestBody WebAlbum album);

    @PostMapping("/web/album/updateAlbum")
    boolean updateAlbum(@RequestBody WebAlbum album);

    @PostMapping("/web/album/addAlbumNoteRelation")
    boolean addAlbumNoteRelation(@RequestBody WebAlbumNoteRelation albumNoteRelation);

    @PostMapping("/web/album/updateAlbumNoteRelation")
    boolean updateAlbumNoteRelation(@RequestBody WebAlbumNoteRelation albumNoteRelation);

    @PostMapping("/web/album/deleteAlbumNoteRelation")
    boolean deleteAlbumNoteRelationByAidAndNid(@RequestParam("aid") Long aid, @RequestParam("nid") Long nid);

    @GetMapping("/web/album/getAlbumNoteRelationByNid")
    List<WebAlbumNoteRelation> getAlbumNoteRelationByNid(Long nid);
}
