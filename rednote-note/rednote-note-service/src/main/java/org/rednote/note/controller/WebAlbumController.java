package org.rednote.note.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.note.api.entity.WebAlbum;
import org.rednote.note.api.entity.WebAlbumNoteRelation;
import org.rednote.note.service.IWebAlbumNoteRelationService;
import org.rednote.note.service.IWebAlbumService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "专辑管理", description = "笔记专辑相关接口")
@RequestMapping("/web/album")
@RestController
@RequiredArgsConstructor
public class WebAlbumController {

    private final IWebAlbumService albumService;
    private final IWebAlbumNoteRelationService albumNoteRelationService;

    /**
     * 以下用于远程调用
     */

    @GetMapping("getAlbumById")
    WebAlbum getAlbumById(@RequestParam("albumId") Long albumId) {
        return albumService.getById(albumId);
    }

    @GetMapping("getAlbumByIds")
    List<WebAlbum> getAlbumByIds(@RequestParam("albumIds") List<Long> albumIds) {
        return albumService.listByIds(albumIds);
    }

    @GetMapping("getAlbumByIdAndType")
    WebAlbum getAlbumByIdAndType(@RequestParam("albumId") Long albumId, @RequestParam("type") Integer type) {
        return albumService.lambdaQuery()
                .eq(WebAlbum::getId, albumId)
                .eq(WebAlbum::getType, type).one();
    }

    @PostMapping("addAlbum")
    Boolean addAlbum(@RequestBody WebAlbum album) {
        return albumService.save(album);
    }

    @PostMapping("updateAlbum")
    Boolean updateAlbum(@RequestBody WebAlbum album) {
        return albumService.saveOrUpdate(album);
    }

    @PostMapping("addAlbumNoteRelation")
    Boolean addAlbumNoteRelation(@RequestBody WebAlbumNoteRelation albumNoteRelation) {
        return albumNoteRelationService.save(albumNoteRelation);
    }

    @PostMapping("updateAlbumNoteRelation")
    Boolean updateAlbumNoteRelation(@RequestBody WebAlbumNoteRelation albumNoteRelation) {
        return albumNoteRelationService.saveOrUpdate(albumNoteRelation);
    }

    @PostMapping("deleteAlbumNoteRelation")
    Boolean deleteAlbumNoteRelationByAidAndNid(@RequestParam("aid") Long aid, @RequestParam("nid") Long nid) {
        return albumNoteRelationService.remove(
                new LambdaQueryWrapper<>(WebAlbumNoteRelation.class)
                        .eq(WebAlbumNoteRelation::getId, aid)
                        .eq(WebAlbumNoteRelation::getNid, nid)
        );
    }

    @GetMapping("getAlbumNoteRelationByNid")
    List<WebAlbumNoteRelation> getAlbumNoteRelationByNid(@RequestParam("nid") Long nid) {
        return albumNoteRelationService.lambdaQuery()
                .eq(WebAlbumNoteRelation::getNid, nid)
                .list();
    }

}
