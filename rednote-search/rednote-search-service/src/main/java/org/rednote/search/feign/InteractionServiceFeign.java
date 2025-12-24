package org.rednote.search.feign;

import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.dto.LikeOrFavoriteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("interaction-service")
public interface InteractionServiceFeign {

    @PostMapping("/web/likeOrFavorite/isLikeOrFavorite")
    Result<Boolean> isLikeOrFavorite(@RequestBody LikeOrFavoriteDTO likeOrFavoriteDTO);
}
