package org.rednote.interaction.feign;

import org.rednote.common.domain.dto.Result;
import org.rednote.user.api.entity.WebUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceFeign {

    @GetMapping("/web/user/getUserById")
    Result<WebUser> getUserById(Long id);

    @GetMapping("/web/user/getUserByIds")
    List<WebUser> getUserByIds(List<Long> ids);

    @PostMapping("/web/user/updateUserById")
    boolean updateUserById(WebUser user);
}
