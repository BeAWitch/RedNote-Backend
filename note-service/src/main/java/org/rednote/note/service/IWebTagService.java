package org.rednote.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.note.domain.entity.WebTag;
import org.rednote.note.domain.vo.TagVO;

/**
 * 标签
 */
public interface IWebTagService extends IService<WebTag> {

    /**
     * 获取热门标签
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<TagVO> getHotTagList(long currentPage, long pageSize);

    /**
     * 根据关键词获取标签
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param keyword     关键词
     */
    Page<TagVO> getTagByKeyword(long currentPage, long pageSize, String keyword);
}
