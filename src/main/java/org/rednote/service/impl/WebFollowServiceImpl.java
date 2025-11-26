package org.rednote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.rednote.domain.entity.WebFollow;
import org.rednote.mapper.WebFollowMapper;
import org.rednote.service.IWebFollowService;
import org.rednote.utils.UserHolder;
import org.springframework.stereotype.Service;

@Service
public class WebFollowServiceImpl extends ServiceImpl<WebFollowMapper, WebFollow> implements IWebFollowService {

    /**
     * 当前用户是否关注
     *
     * @param followerId 关注的用户ID
     */
    @Override
    public boolean isFollow(Long followerId) {
        Long userId = UserHolder.getUserId();
        long count = this.count(new QueryWrapper<WebFollow>().eq("uid", userId).eq("fid", followerId));
        return count > 0;
    }
}
