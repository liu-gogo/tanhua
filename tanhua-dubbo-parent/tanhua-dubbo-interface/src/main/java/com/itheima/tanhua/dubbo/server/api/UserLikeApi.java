package com.itheima.tanhua.dubbo.server.api;



import com.itheima.tanhua.dubbo.server.pojo.UserLike;

import java.util.List;

public interface UserLikeApi {

    /**
     * 保存喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    String saveUserLike(Long userId, Long likeUserId);


    /**
     * 相互喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isMutualLike(Long userId, Long likeUserId);

    /**
     * 删除用户喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean deleteUserLike(Long userId, Long likeUserId);

    /**
     * 相互喜欢的数量
     *
     * @return
     */
    Long queryEachLikeCount(Long userId);

    /**
     * 喜欢数
     *
     * @return
     */
    Long queryLikeCount(Long userId);

    /**
     * 粉丝数
     *
     * @return
     */
    Long queryFanCount(Long userId);

    List<UserLike> queryEachLikeList(Long userId, int page, int pageSize);
    /**
     * 喜欢数
     *
     * @return
     */
    List<UserLike> queryLikeList(Long userId, int page, int pageSize);

    /**
     * 粉丝数
     *
     * @return
     */
    List<UserLike> queryFanList(Long userId, int page, int pageSize);

    /**
     * userId是否喜欢likeUserId
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isLike(Long userId, Long likeUserId);
}
