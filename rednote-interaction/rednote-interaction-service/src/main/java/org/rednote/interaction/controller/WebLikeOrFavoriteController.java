package org.rednote.interaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.dto.LikeOrFavoriteDTO;
import org.rednote.interaction.api.vo.LikeOrFavoriteVO;
import org.rednote.interaction.service.IWebLikeOrFavoriteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "点赞和收藏管理", description = "点赞和收藏相关接口")
@RequestMapping("/web/likeOrFavorite")
@RestController
@RequiredArgsConstructor
public class WebLikeOrFavoriteController {

    private final IWebLikeOrFavoriteService likeOrFavoriteService;

    @Operation(summary = "点赞或收藏", description = "点赞或收藏")
    @PostMapping("likeOrFavoriteByDTO")
    public Result<?> likeOrFavoriteByDTO(
            @Parameter(description = "点赞收藏实体") @RequestBody LikeOrFavoriteDTO likeOrFavoriteDTO) {
        likeOrFavoriteService.likeOrFavoriteByDTO(likeOrFavoriteDTO);
        return Result.ok();
    }

    @Operation(summary = "是否点赞或收藏", description = "是否点赞或收藏")
    @PostMapping("isLikeOrFavorite")
    public Result<?> isLikeOrFavorite(
            @Parameter(description = "点赞收藏实体") @RequestBody LikeOrFavoriteDTO likeOrFavoriteDTO) {
        boolean flag = likeOrFavoriteService.isLikeOrFavorite(likeOrFavoriteDTO);
        return Result.ok(flag);
    }

    @Operation(summary = "获取当前用户最新的点赞和收藏信息", description = "获取当前用户最新的点赞和收藏信息")
    @GetMapping("getLikeAndFavoriteInfo/{currentPage}/{pageSize}")
    public Result<?> getLikeAndFavoriteInfo(
            @Parameter(description = "当前页码") @PathVariable long currentPage,
            @Parameter(description = "每页大小") @PathVariable long pageSize) {
        Page<LikeOrFavoriteVO> pageInfo = likeOrFavoriteService.getLikeAndFavoriteInfo(currentPage, pageSize);
        return Result.ok(pageInfo);
    }
}
