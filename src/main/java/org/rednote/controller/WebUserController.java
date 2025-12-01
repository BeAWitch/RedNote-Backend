package org.rednote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.dto.Result;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.vo.NoteSearchVO;
import org.rednote.service.IWebUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RequestMapping("/web/user")
@RestController
@RequiredArgsConstructor
public class WebUserController {

    private final IWebUserService userService;

    @Operation(summary = "获取用户的动态", description = "获取用户的动态")
    @GetMapping("getTrendByUser/{currentPage}/{pageSize}")
    public Result<?> getTrendByUser(@Parameter(description = "当前页码") @PathVariable long currentPage,
                                    @Parameter(description = "每页大小") @PathVariable long pageSize,
                                    @Parameter(description = "用户 ID") String userId,
                                    @Parameter(description = "类型（1：笔记，2：点赞，3：收藏）") int type) {
        Page<NoteSearchVO> pageInfo = userService.getTrendByUser(currentPage, pageSize, userId, type);
        return Result.ok(pageInfo);
    }

    @Operation(summary = "获取用户信息", description = "获取用户信息")
    @GetMapping("getUserById")
    public Result<?> getUserById(@Parameter(description = "用户 ID") String userId) {
        WebUser user = userService.getById(userId);
        return Result.ok(user);
    }

    @Operation(summary = "更新用户信息", description = "更新用户信息")
    @PostMapping("updateUser")
    public Result<?> updateUser(
            @Parameter(description = "用户数据 JSON 字符串") @RequestParam("userDTO") String userData,
            @Parameter(description = "上传的头像") @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        WebUser updateUser = userService.updateUser(userData, avatar);
        return Result.ok(updateUser);
    }

    @Operation(summary = "查找用户信息", description = "查找用户信息")
    @GetMapping("getUserByKeyword/{currentPage}/{pageSize}")
    public Result<?> getUserByKeyword(@Parameter(description = "当前页码") @PathVariable long currentPage,
                                      @Parameter(description = "每页大小") @PathVariable long pageSize,
                                      @Parameter(description = "关键词") String keyword) {
        Page<WebUser> pageInfo = userService.getUserByKeyword(currentPage, pageSize, keyword);
        return Result.ok(pageInfo);
    }
}
