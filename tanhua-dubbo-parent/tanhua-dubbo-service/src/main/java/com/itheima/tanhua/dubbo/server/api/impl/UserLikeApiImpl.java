package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.tanhua.dubbo.server.api.UserLikeApi;
import com.itheima.tanhua.dubbo.server.pojo.UserLike;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service(version = "1.0.0")
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public String saveUserLike(Long userId, Long likeUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
        if (mongoTemplate.count(query,UserLike.class) > 0){
            return null;
        }

        UserLike userLike = new UserLike();
        userLike.setUserId(userId);
        userLike.setLikeUserId(likeUserId);
        userLike.setId(ObjectId.get());
        userLike.setCreated(System.currentTimeMillis());

        mongoTemplate.save(userLike);
        return userLike.getId().toHexString();
    }

    @Override
    public Boolean isMutualLike(Long userId, Long likeUserId) {
        Criteria criteria1 = Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId);
        Criteria criteria2 = Criteria.where("userId").is(likeUserId).and("likeUserId").is(userId);
        Criteria criteria = new Criteria().orOperator(criteria1,criteria2);
        return mongoTemplate.count(Query.query(criteria), UserLike.class) == 2;
    }

    @Override
    public Boolean deleteUserLike(Long userId, Long likeUserId) {


        Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));

        DeleteResult deleteResult = mongoTemplate.remove(query, UserLike.class);
        return deleteResult.getDeletedCount() == 1;


    }

    @Override
    public Long queryEachLikeCount(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);

        List<Long> likeUserIdList = new ArrayList<>();
        for (UserLike userLike : userLikeList) {
            likeUserIdList.add(userLike.getUserId());
        }

        Query queryCount = Query.query(Criteria.where("userId").in(likeUserIdList).and("likeUserId").is(userId));


        return mongoTemplate.count(query, UserLike.class);
    }

    @Override
    public Long queryLikeCount(Long userId) {

        Query query = Query.query(Criteria
                .where("userId")
                .is(userId));
        return this.mongoTemplate.count(query,UserLike.class);

    }

    @Override
    public Long queryFanCount(Long userId) {
        Query query = Query.query(Criteria
                .where("likeUserId")
                .is(userId));
        return this.mongoTemplate.count(query,UserLike.class);
    }

    @Override
    public List<UserLike> queryEachLikeList(Long userId, int page, int pageSize) {
        Query query = Query.query(Criteria
                .where("userId")
                .is(userId));
        List<UserLike> userLikeList = this.mongoTemplate.find(query,UserLike.class);
        List<Long> likeUserIdList = new ArrayList<>();
        for (UserLike userLike : userLikeList) {
            likeUserIdList.add(userLike.getLikeUserId());
        }
        Query queryCount = Query.query(Criteria
                .where("userId")
                .in(likeUserIdList).and("likeUserId").is(userId));
        queryCount.with(PageRequest.of(page-1,pageSize, Sort.by(Sort.Order.desc("created"))));
        return this.mongoTemplate.find(queryCount,UserLike.class);
    }

    @Override
    public List<UserLike> queryLikeList(Long userId, int page, int pageSize) {
        Query query = Query.query(Criteria
                .where("userId")
                .is(userId));
        query.with(PageRequest.of(page-1,pageSize, Sort.by(Sort.Order.desc("created"))));
        return this.mongoTemplate.find(query,UserLike.class);
    }

    @Override
    public List<UserLike> queryFanList(Long userId, int page, int pageSize) {
        Query query = Query.query(Criteria
                .where("likeUserId")
                .is(userId));
        query.with(PageRequest.of(page-1,pageSize, Sort.by(Sort.Order.desc("created"))));
        return this.mongoTemplate.find(query,UserLike.class);
    }

    @Override
    public Boolean isLike(Long userId, Long likeUserId) {
        Query query = Query.query(Criteria
                .where("userId")
                .is(likeUserId).and("likeUserId").is(userId));
        return this.mongoTemplate.count(query,UserLike.class) > 0;
    }
}
