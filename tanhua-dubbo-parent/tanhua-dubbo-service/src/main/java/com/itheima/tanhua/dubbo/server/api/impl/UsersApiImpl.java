package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.tanhua.dubbo.server.api.UsersApi;
import com.itheima.tanhua.dubbo.server.pojo.Users;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class UsersApiImpl implements UsersApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String saveUser(Users users) {
        Long friendId = users.getFriendId();
        Long userId = users.getUserId();

        if (userId == null || friendId == null){
            return null;
        }

        Criteria criteria = Criteria.where("userId").is(userId).and("friendId").is(friendId);
        Criteria criteria1 = Criteria.where("userId").is(friendId).and("friendId").is(userId);

        Query query = Query.query(criteria.orOperator(criteria1)).limit(1);


        Users users1 = mongoTemplate.findOne(query, Users.class);
        if (users1 != null){
            return users1.getId().toString();
        }


        users.setId(ObjectId.get());
        users.setDate(System.currentTimeMillis());

        mongoTemplate.save(users);



        return users.getId().toString();
    }

    @Override
    public List<Users> findAllUsersList(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return this.mongoTemplate.find(query,Users.class);
    }

    @Override
    public List<Users> findUsersListPage(Long userId, int page, int pageSize) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        PageRequest pageRequest = PageRequest.of(page-1, pageSize);
        query.with(pageRequest).with(Sort.by(Sort.Order.desc("date")));
        return this.mongoTemplate.find(query,Users.class);
    }
}
