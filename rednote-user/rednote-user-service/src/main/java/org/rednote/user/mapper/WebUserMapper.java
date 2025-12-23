package org.rednote.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.rednote.user.api.entity.WebUser;

import java.util.List;

@Mapper
public interface WebUserMapper extends BaseMapper<WebUser> {

    /**
     * 根据条件分页查询角色数据
     *
     * @param user 会员
     * @return 角色数据集合信息
     */
    List<WebUser> getUserList(WebUser user);
}
