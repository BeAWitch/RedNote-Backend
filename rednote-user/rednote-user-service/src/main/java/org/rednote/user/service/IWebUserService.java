package org.rednote.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.user.api.entity.WebUser;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户
 */
public interface IWebUserService extends IService<WebUser> {

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
