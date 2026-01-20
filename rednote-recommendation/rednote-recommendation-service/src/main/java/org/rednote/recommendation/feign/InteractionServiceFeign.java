package org.rednote.recommendation.feign;

import org.rednote.interaction.api.entity.WebComment;
import org.rednote.interaction.api.entity.WebLikeOrFavorite;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "interaction-service")
public interface InteractionServiceFeign {

    @GetMapping("/web/likeOrFavorite/getLikeOrFavoriteByTime")
    List<WebLikeOrFavorite> getLikeOrFavoriteByTime(@RequestParam("time")LocalDateTime time);

    @GetMapping("/web/comment/getCommentByTime")
    List<WebComment> getCommentByTime(@RequestParam("time") LocalDateTime time);
}
