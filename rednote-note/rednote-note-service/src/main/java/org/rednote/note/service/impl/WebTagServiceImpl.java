package org.rednote.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.note.api.entity.WebTag;
import org.rednote.note.api.entity.WebTagNoteRelation;
import org.rednote.note.api.vo.TagVO;
import org.rednote.note.mapper.WebTagMapper;
import org.rednote.note.mapper.WebTagNoteRelationMapper;
import org.rednote.note.service.IWebTagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签
 */
@Service
@RequiredArgsConstructor
public class WebTagServiceImpl extends ServiceImpl<WebTagMapper, WebTag> implements IWebTagService {

    private final WebTagNoteRelationMapper webTagNoteRelationMapper;

    /**
     * 获取热门标签
     */
    @Override
    public Page<TagVO> getHotTagList(long currentPage, long pageSize) {
        return getTagByKeyword(currentPage, pageSize, null);
    }

    /**
     * 根据关键词获取标签
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param keyword     关键词
     */
    @Override
    public Page<TagVO> getTagByKeyword(long currentPage, long pageSize, String keyword) {
        QueryWrapper<WebTag> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like("title", keyword);
        }
        queryWrapper.orderByDesc("like_count");
        List<TagVO> tagVOS = this.page(new Page<>((int) currentPage, (int) pageSize), queryWrapper).getRecords()
                .stream()
                .map(webTag -> BeanUtil.copyProperties(webTag, TagVO.class))
                .toList();
        return new Page<TagVO>().setRecords(tagVOS).setTotal(tagVOS.size());
    }

    @Override
    public List<WebTagNoteRelation> getTagNoteRelationByNid(Long nid) {
        return webTagNoteRelationMapper.selectList(
                new LambdaQueryWrapper<>(WebTagNoteRelation.class).eq(WebTagNoteRelation::getNid, nid)
        );
    }
}
