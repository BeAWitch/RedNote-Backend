package org.rednote.note.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.note.domain.entity.WebNavbar;

import java.util.List;

/**
 * 笔记分类
 */
public interface IWebNavbarService extends IService<WebNavbar> {

    /**
     * 获取树形分类数据
     */
    List<Tree<Long>> getNavbarTreeData();

}
