package com.itheima.tanhua.server.service;


import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.tanhua.dubbo.server.api.RecommendUserApi;

import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendUserService {

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    public RecommendUser findRecommendUserMaxScore(long userId){
        return recommendUserApi.queryMaxScore(userId);
    }

    public List<RecommendUser> queryRecommendUserList(Long id,Integer pageNum,Integer pageSize){
        return recommendUserApi.queryRecommendUserList(id,pageNum, pageSize);
    }

    public RecommendUser findRecommendUser(Long toUserId, Long userId) {
        return recommendUserApi.findRecommendUser(toUserId,userId);
    }
}
