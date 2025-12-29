package org.rednote.note.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.entity.WebComment;
import org.rednote.interaction.api.entity.WebFollow;
import org.rednote.interaction.api.entity.WebLikeOrFavorite;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("interaction-service")
public interface InteractionServiceFeign {

    @GetMapping("/web/follow/isFollow")
    Result<Boolean> isFollow(@RequestParam("followId") Long followId);

    @GetMapping("/web/likeOrFavorite/getLikeOrFavoriteByNidAndUid")
    List<WebLikeOrFavorite> getLikeOrFavoriteByNidAndUid(
            @RequestParam("nid") Long nid,
            @RequestParam("uid") Long uid
    );

    @GetMapping("/web/likeOrFavorite/getLikeOrFavoriteByUidAndTypeOrderByTime")
    Page<WebLikeOrFavorite> getLikeOrFavoriteByUidAndTypeOrderByTime(
            @RequestParam("currentPage") Long currentPage,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam("uid") Long uid,
            @RequestParam("type") Integer type
    );

    @GetMapping("/web/likeOrFavorite/getLikeOrFavoriteByUidAndType")
    List<WebLikeOrFavorite> getLikeOrFavoriteByUidAndType(
            @RequestParam("uid") Long uid,
            @RequestParam("type") Integer type
    );

    @PostMapping("/web/likeOrFavorite/deleteLikeOrFavoriteByObjId")
    Boolean deleteLikeOrFavoriteByObjId(@RequestParam("objId") Long objId);

    @PostMapping("/web/likeOrFavorite/deleteLikeOrFavoriteByObjIds")
    Boolean deleteLikeOrFavoriteByObjIds(@RequestBody List<Long> objIds);

    @PostMapping("/web/comment/deleteCommentByIds")
    Boolean deleteCommentByIds(@RequestBody List<Long> commentIds);

    @GetMapping("/web/comment/getCommentByNid")
    List<WebComment> getCommentByNid(@RequestParam("nid") Long nid);

    @GetMapping("/web/follow/getFollowByFid")
    List<WebFollow> getFollowByFid(@RequestParam("fid") Long fid);

    @PostMapping("/web/chat/increaseUncheckedMessageCount")
    void increaseUncheckedMessageCount(
            @RequestParam("type")UncheckedMessageEnum type,
            @RequestParam("uid") Long uid,
            @RequestParam("count") long count);
}
