package org.rednote.note.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.rednote.note.domain.entity.WebNavbar;
import org.rednote.note.mapper.WebNavbarMapper;
import org.rednote.note.service.IWebNavbarService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔记分类
 */
@Service
public class WebNavbarServiceImpl extends ServiceImpl<WebNavbarMapper, WebNavbar> implements IWebNavbarService {

    /**
     * 获取树形分类数据
     */
    @Override
    public List<Tree<Long>> getNavbarTreeData() {
        List<WebNavbar> list = this.list(new QueryWrapper<WebNavbar>().orderByAsc("sort"));
        return convertToTree(list);
    }

    private List<Tree<Long>> convertToTree(List<WebNavbar> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 配置
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("pid");
        config.setNameKey("title");
        config.setDeep(3);
        config.setChildrenKey("children");
        config.setWeightKey("sort");

        // 转树
        return TreeUtil.build(list, 0L, config, ((object, treeNode) -> {
            treeNode.putExtra("id", object.getId());
            treeNode.putExtra("pid", object.getPid());
            treeNode.putExtra("title", object.getTitle());
            treeNode.putExtra("likeCount", object.getLikeCount());
            treeNode.putExtra("sort", object.getSort());
        }));
    }
}
