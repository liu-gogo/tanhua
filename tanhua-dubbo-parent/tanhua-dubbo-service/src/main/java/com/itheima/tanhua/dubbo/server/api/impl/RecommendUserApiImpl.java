package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.tanhua.dubbo.server.api.RecommendUserApi;


import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


import java.util.List;

@Service(version = "1.0.0")
public  class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public RecommendUser queryMaxScore(long userId) {


        Query query = Query.query(Criteria.where("toUserId").is(userId));
        query.with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    @Override

    public List<RecommendUser> queryRecommendUserList(long userId, int pageNum, int pageSize) {
        Query query = Query.query(Criteria.where("toUserId").is(userId));
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Order.desc("score")));
        query.with(pageRequest);
        return mongoTemplate.find(query, RecommendUser.class);
    }

    @Override
    public RecommendUser findRecommendUser(Long toUserId, Long userId) {
        Query query = Query.query(Criteria.where("toUserId").is(toUserId).and("userId").is(userId));
        return this.mongoTemplate.findOne(query,RecommendUser.class);
    }
}
