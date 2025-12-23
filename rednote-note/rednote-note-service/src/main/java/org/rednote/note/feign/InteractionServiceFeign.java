package org.rednote.note.feign;

import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.entity.WebComment;
import org.rednote.interaction.api.entity.WebFollow;
import org.rednote.interaction.api.entity.WebLikeOrFavorite;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("interaction-service")
public interface InteractionServiceFeign {

    @GetMapping("/web/follow/isFollow")
    Result<Boolean> isFollow(Long followId);

    @GetMapping("/web/likeOrFavorite/getLikeOrFavoriteByNidAndUid")
    List<WebLikeOrFavorite> getLikeOrFavoriteByNidAndUid(
            @RequestParam("nid") Long nid, @RequestParam("uid") Long uid);

    @PostMapping("/web/likeOrFavorite/deleteLikeOrFavoriteByObjId")
    boolean deleteLikeOrFavoriteByObjId(Long objId);

    @PostMapping("/web/likeOrFavorite/deleteLikeOrFavoriteByObjIds")
    boolean deleteLikeOrFavoriteByObjIds(List<Long> objIds);

    @PostMapping("/web/comment/deleteCommentByIds")
    boolean deleteCommentByIds(List<Long> commentIds);

    @GetMapping("/web/comment/getCommentByNid")
    List<WebComment> getCommentByNid(Long nid);

    @GetMapping("/web/follow/getFollowByFid")
    List<WebFollow> getFollowByFid(Long fid);

    @PostMapping("increaseUncheckedMessageCount")
    void increaseUncheckedMessageCount(
            @RequestParam("type")UncheckedMessageEnum type,
            @RequestParam("uid") Long uid,
            @RequestParam("count") long count);
}
