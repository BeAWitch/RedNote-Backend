package org.rednote.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.interaction.api.dto.LikeOrFavoriteDTO;
import org.rednote.interaction.api.entity.WebLikeOrFavorite;
import org.rednote.interaction.api.vo.LikeOrFavoriteVO;

/**
 * 点赞/收藏
 */
public interface IWebLikeOrFavoriteService extends IService<WebLikeOrFavorite> {

    /**
     * 点赞或收藏
     *
     * @param likeOrFavoriteDTO 点赞收藏
     */
    void likeOrFavoriteByDTO(LikeOrFavoriteDTO likeOrFavoriteDTO);

    /**
     * 是否点赞或收藏
     *
     * @param likeOrFavoriteDTO 点赞收藏
     */
    boolean isLikeOrFavorite(LikeOrFavoriteDTO likeOrFavoriteDTO);

    /**
     * 获取当前用户最新的点赞和收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<LikeOrFavoriteVO> getLikeAndFavoriteInfo(long currentPage, long pageSize);
}
