package org.rednote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.vo.NoteSearchVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户
 */
public interface IWebUserService extends IService<WebUser> {

    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型（1：笔记，2：点赞，3：收藏）
     */
    Page<NoteSearchVO> getTrendByUser(long currentPage, long pageSize, String userId, int type);

    /**
     * 更新用户信息
     *
     * @param userData 用户数据 JSON 字符串
     * @param avatar   上传的头像
     */
    WebUser updateUser(String userData, MultipartFile avatar);

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     */
    Page<WebUser> getUserByKeyword(long currentPage, long pageSize, String keyword);

}
