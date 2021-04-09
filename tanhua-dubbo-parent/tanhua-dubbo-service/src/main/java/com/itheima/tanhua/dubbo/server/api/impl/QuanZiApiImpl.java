package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.itheima.tanhua.dubbo.server.pojo.*;

import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.service.IDService;
import com.itheima.tanhua.dubbo.server.vo.CommentTypeEnum;
import com.itheima.tanhua.dubbo.server.vo.PageInfo;
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
public class QuanZiApiImpl implements QuanZiApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IDService idService;
    @Override
    public String savePublish(Long userId, Publish publish) {
        /**
         * 1. 动态表
         * 2. 相册表
         * 3. 时间线表
         */
        //1. 动态表  一般接口的第一步实现 为参数的有效性校验
        // private ObjectId id; //主键id  肯定是service服务来去定义，参数中publish 肯定没id
        if (userId == null || publish == null){
            return null;
        }
        publish.setId(ObjectId.get());

        publish.setPid(idService.created("PUBLISH",publish.getId().toHexString()));
        publish.setSeeType(1);//app并没有用到
        long created = System.currentTimeMillis();
        publish.setCreated(created);
        mongoTemplate.save(publish);


        //2. 相册表
        Album album = new Album();
        album.setPublishId(publish.getId());
        album.setId(ObjectId.get());
        album.setUserId(userId);
        album.setCreated(created);
        mongoTemplate.save(album,"quanzi_album_"+userId);
        //3. 时间线表  先要查询好友，存入好友的时间线表中
        //   这个时间线表的保存 有没有问题？ 微信好友上限 5000人
        //   消息队列
        Query query = Query.query(Criteria.where("userId").is(userId));
        List<Users> users = this.mongoTemplate.find(query, Users.class);
        for (Users user : users) {
            TimeLine timeLine = new TimeLine();
            timeLine.setUserId(userId);
            timeLine.setPublishId(publish.getId());
            timeLine.setId(ObjectId.get());
            timeLine.setDate(created);
            this.mongoTemplate.save(timeLine,"quanzi_time_line_"+user.getFriendId());
        }

        return publish.getId().toHexString();
    }

    @Override
    public List<Publish> queryPublishList(Long userId, Integer pageNum, Integer pageSize,boolean isRecommend) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Query query = new Query().with(pageRequest).with(Sort.by(Sort.Order.desc("date")));


        String recommendName = "quanzi_time_line_" + userId;
        if(isRecommend){
            recommendName = "quanzi_time_line_recommend";
        }
        List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class,recommendName );

        List<ObjectId> publishIdList = new ArrayList<>();

        for (TimeLine timeLine : timeLineList) {
            publishIdList.add(timeLine.getPublishId());

        }

        Query queryPublishId = Query.query(Criteria.where("id").in(publishIdList)).with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishes = mongoTemplate.find(queryPublishId, Publish.class);

        return publishes;
    }

    @Override
    public boolean saveComment(Long userId, String publishId, String content, int commentType) {

        if(!(CommentTypeEnum.COMMENT.getCode() == commentType)){
            Query query = Query.query(Criteria.where("userId").is(userId)
                    .and("publishId").is(new ObjectId(publishId))
                    .and("commentType").is(commentType));
            long count = mongoTemplate.count(query, Comment.class);
            if(count >0){
            return true;
        }
        }
        Comment comment = new Comment();
        comment.setId(ObjectId.get());
        comment.setCommentType(commentType);
        comment.setPublishId(new ObjectId(publishId));

        Long publishUserId = -1L;
        Query query = Query.query(Criteria.where("id").is(publishId));
        Publish publish = mongoTemplate.findOne(query, Publish.class);
        if (publish!=null){
            publishUserId = publish.getUserId();
        }else {
            Comment comment1 = mongoTemplate.findOne(Query.query(Criteria.where("id").is(publishId)), Comment.class);
            if(comment1 != null){
                publishUserId = comment1.getUserId();
            }else {
                Video video = mongoTemplate.findOne(Query.query(Criteria.where("id").is(publishId)), Video.class);
                if (video != null){
                    publishUserId = video.getUserId();
                }
            }
        }

        comment.setPublishUserId(publishUserId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreated(System.currentTimeMillis());
        comment.setIsParent(true);

        mongoTemplate.save(comment);

        return true;
    }

    @Override
    public boolean removeComment(Long userId, String publishId, int commentType) {
        Query query = Query.query(Criteria.where("userId").is(userId)
                .and("publishId").is(publishId)
                .and("commentType").is(commentType));


        mongoTemplate.remove(query, Comment.class);
        return false;
    }


    @Override
    public Publish queryPublish(String publishId) {
        Query query = Query.query(Criteria.where("id").is(new ObjectId(publishId)));
        Publish publish = mongoTemplate.findOne(query, Publish.class);
        return publish;
    }

    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {

        PageRequest pageRequest = PageRequest.of(page-1,pageSize);
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(CommentTypeEnum.COMMENT.getCode()));

        query.with(pageRequest).with(Sort.by(Sort.Order.desc("created")));

        List<Comment> commentList = mongoTemplate.find(query, Comment.class);

        Query query1 = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(CommentTypeEnum.COMMENT.getCode()));

        long count = mongoTemplate.count(query, Comment.class);
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setList(commentList);
        pageInfo.setCount(count);
        return pageInfo;
    }



    @Override
    public long queryCommentCount(String publishId, int commentType) {
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType));
        return this.mongoTemplate.count(query, Comment.class);
    }


    @Override
    public List<Comment> queryCommentListByPublishUserId(Long userId, int commentType) {
        Query query = Query.query(Criteria.where("publishUserId").is(userId)
                .and("commentType").is(commentType));
        return this.mongoTemplate.find(query,Comment.class);
    }

    @Override
    public List<Publish> queryPublishByPids(List<Long> pidList) {
        Query queryPublish = Query.query(Criteria.where("pid").in(pidList)).with(Sort.by(Sort.Order.desc("created")));
        return this.mongoTemplate.find(queryPublish,Publish.class);
    }

    @Override
    public List<Publish> queryAlbum(Long userId, int page, int pageSize) {
        //相册表
        Query query1 = new Query();
        PageRequest pageRequest = PageRequest.of(page-1, pageSize,Sort.by(Sort.Order.desc("created")));
        query1.with(pageRequest);
        List<Album> albums = this.mongoTemplate.find(query1, Album.class, "quanzi_album_" + userId);

        if (CollectionUtils.isEmpty(albums)){
            return new ArrayList<>();
        }
        List<ObjectId> publishIdList = new ArrayList<>();

        for (Album album : albums) {
            publishIdList.add(album.getPublishId());
        }
        //in ()
        Query query = Query.query(Criteria.where("id").in(publishIdList));
        return this.mongoTemplate.find(query,Publish.class);
    }
}
