package org.rednote.search.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.common.domain.dto.Result;
import org.rednote.user.api.entity.WebUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceFeign {

    @GetMapping("/web/user/getUserById")
    Result<WebUser> getUserById(Long id);

    @GetMapping("/web/user/selectUserPage")
    Page<WebUser> selectUserPage(
            @RequestParam("page") Page<WebUser> page,
            @RequestParam("queryWrapper") LambdaQueryWrapper<WebUser> queryWrapper);
}
