package org.rednote.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "recommendation-service")
public interface RecommendationServiceFeign {

    @GetMapping("/web/recommendation/recommendNotes")
    List<Long> recommendNotes(@RequestParam("uid") Long uid, @RequestParam("count") int count);
}
