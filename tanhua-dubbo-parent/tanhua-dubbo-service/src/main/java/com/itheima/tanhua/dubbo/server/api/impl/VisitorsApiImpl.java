package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.tanhua.dubbo.server.api.VisitorsApi;
import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import com.itheima.tanhua.dubbo.server.pojo.Visitors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class VisitorsApiImpl implements VisitorsApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String saveVisitor(Visitors visitors) {
        visitors.setId(ObjectId.get());
        visitors.setDate(System.currentTimeMillis());

        return mongoTemplate.save(visitors).getId().toHexString();
    }

    @Override
    public List<Visitors> queryVisitorsList(Long userId, Long date, int num) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("date").gt(date));
        PageRequest pageRequest = PageRequest.of(0, num, Sort.by(Sort.Order.by("date")));
        query.with(pageRequest);


        List<Visitors> visitorsList = mongoTemplate.find(query, Visitors.class);
        for (Visitors visitors : visitorsList) {
            Query query1 = Query.query(Criteria.where("userId").is(userId).and("toUserId").is(visitors.getVisitorUserId()));
            RecommendUser recommendUser = mongoTemplate.findOne(query1, RecommendUser.class);
            if (recommendUser == null){
                visitors.setScore(30d);
            }else {
                    visitors.setScore(recommendUser.getScore());
            }



        }
        return visitorsList;
    }

    @Override
    public List<Visitors> queryVisitorsPage(Long userId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page-1, pageSize,Sort.by(Sort.Order.desc("date")));
        Query query = Query.query(Criteria.where("userId").is(userId));
        query.with(pageRequest);
        return this.mongoTemplate.find(query,Visitors.class);
    }


}
