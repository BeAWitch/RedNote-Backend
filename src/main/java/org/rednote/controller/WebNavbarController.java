package org.rednote.controller;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.dto.Result;
import org.rednote.service.IWebNavbarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        List<Tree<String>> navbarList = navbarService.getNavbarTreeData();
        return Result.ok(navbarList);
    }
}
