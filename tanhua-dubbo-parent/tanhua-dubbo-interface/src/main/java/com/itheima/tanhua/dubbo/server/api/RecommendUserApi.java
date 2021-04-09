package com.itheima.tanhua.dubbo.server.api;


import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;

import java.util.List;

public interface RecommendUserApi {

    /**
     * 今日佳人
     * 从mongo recommend_user表中查询分数最大的匹配用户
     * 查询最大分数的推荐用户
     * */
    RecommendUser queryMaxScore(long userId);

    /**
     * 分页查询
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<RecommendUser> queryRecommendUserList(long userId, int pageNum, int pageSize);


    RecommendUser findRecommendUser(Long toUserId, Long userId);
}
