package org.rednote.service;

/**
 * 关注
 */
public interface IWebFollowService {

    /**
     * 当前用户是否关注
     *
     * @param followerId 关注的用户ID
     */
    boolean isFollow(String followerId);
}
