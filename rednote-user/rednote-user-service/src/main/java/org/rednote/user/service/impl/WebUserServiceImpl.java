package org.rednote.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.common.exception.RedNoteException;
import org.rednote.user.api.entity.WebUser;
import org.rednote.user.feign.OssServiceFeign;
import org.rednote.user.mapper.WebUserMapper;
import org.rednote.user.service.IWebUserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户
 */
@Service
@RequiredArgsConstructor
public class WebUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IWebUserService {

    private final WebUserMapper userMapper;
    private final OssServiceFeign ossServiceFeign;

    /**
     * 更新用户信息
     *
     * @param userData 用户数据 JSON 字符串
     * @param avatar   上传的头像
     */
    @Override
    public WebUser updateUser(String userData, MultipartFile avatar) {
        WebUser user = JSON.parseObject(userData, WebUser.class);

        WebUser webUser = userMapper.selectById(user.getId());
        if (ObjectUtil.isEmpty(webUser)) {
            throw new RedNoteException("用户信息更新失败，该用户不存在！");
        }

        webUser.setUsername(user.getUsername());
        webUser.setDescription(user.getDescription());
        webUser.setTags(user.getTags());

        // 上传头像
        String avatarUrl = ossServiceFeign.uploadFile(avatar);
        if (StrUtil.isEmpty(avatarUrl)) {
            throw new RedNoteException("头像上传失败！");
        }
        webUser.setAvatar(avatarUrl);

        userMapper.updateById(webUser);

        return webUser;
    }

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     */
    @Override
    public Page<WebUser> getUserByKeyword(long currentPage, long pageSize, String keyword) {
        Page<WebUser> resultPage;
        resultPage = userMapper.selectPage(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebUser>().like("username", keyword));
        return resultPage;
    }
}
