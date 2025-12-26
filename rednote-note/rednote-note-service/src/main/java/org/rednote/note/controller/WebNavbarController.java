package org.rednote.note.controller;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.note.api.entity.WebNavbar;
import org.rednote.note.service.IWebNavbarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "分类管理", description = "笔记分类相关接口")
@RequestMapping("/web/category")
@RestController
@RequiredArgsConstructor
public class WebNavbarController {

    private final IWebNavbarService navbarService;

    @Operation(summary = "获取树形分类数据", description = "获取树形分类数据")
    @GetMapping("getCategoryTreeData")
    public Result<?> getCategoryTreeData() {
        List<Tree<Long>> navbarList = navbarService.getNavbarTreeData();
        return Result.ok(navbarList);
    }

    /**
     * 以下用于远程调用
     */

    @Operation(hidden = true)
    @GetMapping("selectCategoryList")
    public List<WebNavbar> selectCategoryListByKeyword(@RequestParam("keyword") String keyword) {
        return navbarService.list(new LambdaQueryWrapper<>(WebNavbar.class).like(WebNavbar::getTitle, keyword));
    }

    @Operation(hidden = true)
    @GetMapping("getCategoryById")
    public WebNavbar getCategoryById(@RequestParam("id") Long id) {
        return navbarService.getById(id);
    }
}
