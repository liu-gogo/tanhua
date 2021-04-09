package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.tanhua.dubbo.server.api.VideoApi;
import com.itheima.tanhua.dubbo.server.pojo.FollowUser;
import com.itheima.tanhua.dubbo.server.pojo.Video;
import com.itheima.tanhua.dubbo.server.service.IDService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class VideoApiImpl implements VideoApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IDService idService;

    @Override
    public Boolean saveVideo(Video video) {
          if ( video.getUserId() == null){
              return false;
          }
          video.setId(ObjectId.get());
          video.setCreated(System.currentTimeMillis());

          video.setVid(idService.created("video", video.getId().toHexString()));

          mongoTemplate.save(video);
        return true;
    }

    @Override
    public List<Video> queryVideoList(Integer pageNum, Integer pageSize) {
        Query query = new Query();
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        query.with(pageRequest).with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query, Video.class);
    }

    @Override
    public boolean saveFollowUser(Long userId, Long followUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));

        long count = mongoTemplate.count(query, FollowUser.class);

        if (count <= 0){
            FollowUser followUser = new FollowUser();
            followUser.setId(ObjectId.get());
            followUser.setFollowUserId(followUserId);
            followUser.setCreated(System.currentTimeMillis());
            followUser.setUserId(userId);
            mongoTemplate.save(followUser);

        }
        return true;
    }

    @Override
    public boolean removeFollowUser(Long userId, Long followUserId) {

        Query query = Query.query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));
        mongoTemplate.remove(query, FollowUser.class);
        return true;
    }
}
