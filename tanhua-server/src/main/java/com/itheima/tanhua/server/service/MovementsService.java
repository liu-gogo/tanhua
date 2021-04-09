package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;

import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.api.VisitorsApi;
import com.itheima.tanhua.dubbo.server.pojo.Publish;
import com.itheima.tanhua.dubbo.server.pojo.Visitors;
import com.itheima.tanhua.dubbo.server.vo.CommentTypeEnum;
import com.itheima.tanhua.server.utils.RelativeDateFormat;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.Movements;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.PicUploadResult;
import com.itheima.tanhua.server.vo.VisitorsVo;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.util.*;

@Service
public class MovementsService {

    @Autowired
    private SSOService ssoService;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String COMMENT_HAS_LOVE = "COMMENT_HAS_LOVE_";
    private static final String COMMENT_LOVE_COUNT = "COMMENT_LOVE_COUNT_";

    public static final String COMMENT_HAS_LIKE = "COMMENT_HAS_LIKE_";
    public static final String COMMENT_LIKE_COUNT = "COMMENT_LIKE_COUNT_";

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    /**
     * 发布动态
     * @param textContent
     * @param location
     * @param latitude
     * @param longitude
     * @param multipartFile
     * @return
     */
    public String savePublish(String textContent,
                               String location,
                               String latitude,
                               String longitude,
                               MultipartFile[] multipartFile) {
        /**
         * 构建Publish 对象
         * userId token校验 获取userId
         *
         */

        User user = UserThreadLocal.get();

        Publish publish = new Publish();

        publish.setLocationName(location);
        publish.setLongitude(longitude);
        publish.setLatitude(latitude);
        publish.setText(textContent);
        publish.setUserId(user.getId());

        if (multipartFile != null){
            List<String> medias = new ArrayList<>();
            for (MultipartFile file : multipartFile) {
                PicUploadResult upload = picUploadService.upload(file);
                if (upload.isSuccess()){
                    medias.add(upload.getImageUrl());
                }
                //如果产品认为 只要一张图片上传失败 那么逻辑就失败
            }
            publish.setMedias(medias);
        }
        String savePublish = quanZiApi.savePublish(user.getId(), publish);
        return savePublish;
    }

    public PageResult queryPublishList(int pageNum, int pageSize, Boolean isRecommend) {
        PageResult pageResult = new PageResult();
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);
        //app端不需要总条数和总数据数
        pageResult.setCounts(0);
        pageResult.setPages(0);

        User user = UserThreadLocal.get();

        List<Publish> publishList = new ArrayList<>();

        if(isRecommend){

            String redisStrPid = redisTemplate.opsForValue().get("QUANZI_PUBLISH_RECOMMEND_" + user.getId());
            if (StringUtils.isEmpty(redisStrPid)){

                redisStrPid = "100018,100049,100029,100048,100023,100091,100042";

        }
            String[] pidStrList = StringUtils.split(redisStrPid, ",");
            int length = pidStrList.length;
            int start = (pageNum - 1) * pageSize;
            if(start < length){
                int end = start + pageSize - 1;

                if (end >= length){
                    end = length - 1 ;
                }

                List<Long> pidList = new ArrayList<>();
                for (int i=start;i<= end;i++){
                    pidList.add(Long.parseLong(pidStrList[i]));
                }
                publishList = quanZiApi.queryPublishByPids(pidList);
            }


        }else {
            publishList = quanZiApi.queryPublishList(user.getId(), pageNum, pageSize, isRecommend);
        }

        List<Long> userIdList = new ArrayList<>();


        List<Movements> movementsList = new ArrayList<>();
        for (Publish publish : publishList) {
            if(!(userIdList.contains(publish.getUserId()))){
                userIdList.add(publish.getUserId());
            }
            Movements movements = new Movements();

            movements.setId(publish.getId().toHexString());
            movements.setImageContent(publish.getMedias().toArray(new String[]{}));
            movements.setTextContent(publish.getText());
            movements.setUserId(publish.getUserId());
            movements.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

            movementsList.add(movements);
        }

        List<UserInfo> userInfoList = ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();

        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }
        for (Movements movements : movementsList) {
            UserInfo userInfo = map.get(movements.getUserId());
            if (userInfo != null){
                movements.setAge(userInfo.getAge());
                movements.setAvatar(userInfo.getLogo());
                movements.setGender(userInfo.getSex().name().toLowerCase());
                movements.setNickname(userInfo.getNickName());
                movements.setTags(StringUtils.split(userInfo.getTags(), ','));

                String publishId = movements.getId();

                long commentCount = this.quanZiApi.queryCommentCount(publishId, CommentTypeEnum.COMMENT.getCode());
                movements.setCommentCount((int) commentCount);

                movements.setDistance("1.2公里"); //TODO 距离
                //如果每次查询列表的时候，都去mongo中查询一遍，性能消耗过大，响应变慢
                //优化策略，每次点赞和喜欢之后，把数据 保存到redis当中,在这个地方 就可以从redis获取了
                //?
                movements.setHasLiked(this.redisTemplate.hasKey(COMMENT_HAS_LIKE+movements.getUserId()+"_"+publishId) ? 1:0); //TODO 是否点赞（1是，0否）
                movements.setHasLoved(this.redisTemplate.hasKey(COMMENT_HAS_LOVE+movements.getUserId()+"_"+publishId) ? 1:0); //TODO 是否喜欢（1是，0否）


                String likeCount = (String) this.redisTemplate.opsForValue().get(COMMENT_LIKE_COUNT+publishId);
                if (StringUtils.isEmpty(likeCount)){
                    movements.setLikeCount(0);
                }else {
                    movements.setLikeCount(Integer.parseInt(likeCount));
                }

//                movements.setLikeCount((int) likeCount); //TODO 点赞数

                String loveCount = (String)this.redisTemplate.opsForValue().get(COMMENT_LOVE_COUNT+publishId);
                if (StringUtils.isEmpty(loveCount)){
                    movements.setLoveCount(0);
                }else {
                    movements.setLoveCount(Integer.parseInt(loveCount));
                }
            }
        }
        pageResult.setItems(movementsList);
        return pageResult;
    }


    /**
     * 动态喜欢
     * @param publishId
     * @return
     */
    public long love(String publishId) {
        /**
         * 1. 拿到登录用户 获取userId
         * 2. 调用dubbo服务，保存喜欢到评论表中
         * 3. redis写入 userId_publishId_love value 1 代表 此用户已经喜欢过此动态了
         * 4. redis写入 此动态的喜欢数 publishId_love_count  100
         */
        User user = UserThreadLocal.get();
        this.quanZiApi.saveComment(user.getId(),publishId,null,CommentTypeEnum.LOVE.getCode());

        this.redisTemplate.opsForValue().set(COMMENT_HAS_LOVE+user.getId()+"_"+publishId,"1");

        Long loveCount = 0L;
        String loveCountStr = (String) this.redisTemplate.opsForValue().get(COMMENT_LOVE_COUNT + publishId);

        if (StringUtils.isEmpty(loveCountStr)){
            //从mongo表查询 赋值
            loveCount = this.quanZiApi.queryCommentCount(publishId, CommentTypeEnum.LOVE.getCode());
            this.redisTemplate.opsForValue().set(COMMENT_LOVE_COUNT + publishId,String.valueOf(loveCount));
        }else{
            //自增 +1
            loveCount = this.redisTemplate.opsForValue().increment(COMMENT_LOVE_COUNT + publishId);
        }

        return loveCount;
    }

    public long like(String publishId) {
        /**
         * 1. 拿到登录用户 获取userId
         * 2. 调用dubbo服务，保存喜欢到评论表中
         * 3. redis写入 userId_publishId_love value 1 代表 此用户已经喜欢过此动态了
         * 4. redis写入 此动态的喜欢数 publishId_love_count  100
         */
        User user = UserThreadLocal.get();
        this.quanZiApi.saveComment(user.getId(),publishId,null,CommentTypeEnum.LIKE.getCode());

        this.redisTemplate.opsForValue().set(COMMENT_HAS_LIKE+user.getId()+"_"+publishId,"1");

        Long likeCount = 0L;
        String likeCountStr = (String) this.redisTemplate.opsForValue().get(COMMENT_LIKE_COUNT + publishId);

        if (StringUtils.isEmpty(likeCountStr)){
            //从mongo表查询 赋值
            likeCount = this.quanZiApi.queryCommentCount(publishId, CommentTypeEnum.LIKE.getCode());
            this.redisTemplate.opsForValue().set(COMMENT_LIKE_COUNT + publishId,String.valueOf(likeCount));
        }else{
            //自增 +1
            likeCount = this.redisTemplate.opsForValue().increment(COMMENT_LIKE_COUNT + publishId);
        }

        return likeCount;
    }

    public long dislike(String publishId) {
        /**
         * 1. 用户id获取
         * 2. 调用dubbo服务 删除评论表中的数据
         * 3. redis数据 清除
         *      1. 是否点赞
         *      2. 点赞总数 减一
         */
        User user = UserThreadLocal.get();
        this.quanZiApi.removeComment(user.getId(),publishId,CommentTypeEnum.LIKE.getCode());
        this.redisTemplate.delete(COMMENT_HAS_LIKE+user.getId()+"_"+publishId);
        Long decrement = this.redisTemplate.opsForValue().decrement(COMMENT_LIKE_COUNT + publishId);
        return decrement;
    }

    public long unlove(String publishId) {
        /**
         * 1. 用户id获取
         * 2. 调用dubbo服务 删除评论表中的数据
         * 3. redis数据 清除
         *      1. 是否点赞
         *      2. 点赞总数 减一
         */
        User user = UserThreadLocal.get();
        this.quanZiApi.removeComment(user.getId(),publishId,CommentTypeEnum.LOVE.getCode());
        this.redisTemplate.delete(COMMENT_HAS_LOVE+user.getId()+"_"+publishId);
        Long decrement = this.redisTemplate.opsForValue().decrement(COMMENT_LOVE_COUNT + publishId);
        return decrement;
    }

    public Movements queryPublish(String publishId) {
        /**
         * 1. userId
         * 2. dubbo调用 获取Publish对象
         * 3. Movements
         * 4. userInfo + Publish + 评论
         */
//        User user = UserThreadLocal.get();
        Publish publish =  this.quanZiApi.queryPublish(publishId);

        Movements movements = new Movements();
        movements.setId(publish.getId().toHexString());
        movements.setImageContent(publish.getMedias().toArray(new String[]{}));
        movements.setTextContent(publish.getText());
        movements.setUserId(publish.getUserId());
        movements.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));//多少分钟之前，多少小时之前

        Long userId = publish.getUserId();

        UserInfo userInfo = this.ssoService.findUserInfoByUserId(userId);
        movements.setAge(userInfo.getAge());
        movements.setAvatar(userInfo.getLogo());
        movements.setGender(userInfo.getSex().name().toLowerCase());
        movements.setNickname(userInfo.getNickName());
        movements.setTags(StringUtils.split(userInfo.getTags(), ','));
        long commentCount = this.quanZiApi.queryCommentCount(publishId, CommentTypeEnum.COMMENT.getCode());
        movements.setCommentCount((int) commentCount);
        movements.setDistance("1.2公里"); //TODO 距离
        //如果每次查询列表的时候，都去mongo中查询一遍，性能消耗过大，响应变慢
        //优化策略，每次点赞和喜欢之后，把数据 保存到redis当中,在这个地方 就可以从redis获取了
        //?
        movements.setHasLiked(this.redisTemplate.hasKey(COMMENT_HAS_LIKE+movements.getUserId()+"_"+publishId) ? 1:0); //TODO 是否点赞（1是，0否）
        movements.setHasLoved(this.redisTemplate.hasKey(COMMENT_HAS_LOVE+movements.getUserId()+"_"+publishId) ? 1:0); //TODO 是否喜欢（1是，0否）


        String likeCount = (String) this.redisTemplate.opsForValue().get(COMMENT_LIKE_COUNT+publishId);
        if (StringUtils.isEmpty(likeCount)){
            movements.setLikeCount(0);
        }else {
            movements.setLikeCount(Integer.parseInt(likeCount));
        }
//                movements.setLikeCount((int) likeCount); //TODO 点赞数
        String loveCount = (String) this.redisTemplate.opsForValue().get(COMMENT_LOVE_COUNT+publishId);
        if (StringUtils.isEmpty(loveCount)){
            movements.setLoveCount(0);
        }else {
            movements.setLoveCount(Integer.parseInt(loveCount));
        }

        return movements;
    }


    public List<VisitorsVo> queryVisitors() {
        User user = UserThreadLocal.get();

        String redisDate = redisTemplate.opsForValue().get("VISITORS_" + user.getId());
        Long date = 0L;

        if (StringUtils.isNotEmpty(redisDate)){
            date = Long.parseLong(redisDate);
        }

        List<Visitors> visitorsList = visitorsApi.queryVisitorsList(user.getId(), date, 5);

        if (CollectionUtils.isEmpty(visitorsList)){
            return Collections.emptyList();
        }

        List<Long> userIdList = new ArrayList<>();

        List<VisitorsVo> visitorsVoList = new ArrayList<>();
        for (Visitors visitors : visitorsList) {
            userIdList.add(visitors.getVisitorUserId());
            VisitorsVo visitorsVo = new VisitorsVo();
            visitorsVo.setId(visitors.getVisitorUserId());
            visitorsVo.setFateValue(visitors.getScore().intValue());
            visitorsVoList.add(visitorsVo);
        }

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }
        for (VisitorsVo visitorsVo : visitorsVoList) {
            UserInfo userInfo = map.get(visitorsVo.getId());
            if (userInfo != null){
                visitorsVo.setAvatar(userInfo.getLogo());
                visitorsVo.setNickname(userInfo.getNickName());
                visitorsVo.setTags(StringUtils.split(userInfo.getTags(), ','));
                visitorsVo.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
                visitorsVo.setAge(userInfo.getAge());
            }
        }
        return visitorsVoList;
    }

    public PageResult all(int page, int pageSize, Long userId) {
        /**
         * 1. 登录用户 id
         * 2. List<Publish> 用户的相册，之前用户的动态信息 存在了相册表中 quanzi_ablum_userId
         * 3. 后续的逻辑 之前已经实现过了
         */
        User user = UserThreadLocal.get();

        List<Publish> publishList = quanZiApi.queryAlbum(userId,page,pageSize);

        PageResult pageResult = new PageResult();
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        pageResult.setCounts(0);
        pageResult.setPages(0);
        if (CollectionUtils.isEmpty(publishList)){
            return  pageResult;
        }
        // 3. 获取 userIdList
        List<Long> userIdList = new ArrayList<>();
        //4. List<Movements>
        List<Movements> movementsList = new ArrayList<>();
        for (Publish publish : publishList) {
            if (!userIdList.contains(publish.getUserId())) {
                userIdList.add(publish.getUserId());
            }
            Movements movements = new Movements();
            movements.setId(publish.getId().toHexString());
            movements.setImageContent(publish.getMedias().toArray(new String[]{}));
            movements.setTextContent(publish.getText());
            movements.setUserId(publish.getUserId());
            movements.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));//多少分钟之前，多少小时之前

            movementsList.add(movements);
        }

        //5. 构建Movements要userInfo信息 根据userIdList去查询用户信息列表

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }
        for (Movements movements : movementsList) {
            UserInfo userInfo = map.get(movements.getUserId());
            if (userInfo != null) {
                movements.setAge(userInfo.getAge());
                movements.setAvatar(userInfo.getLogo());
                movements.setGender(userInfo.getSex().name().toLowerCase());
                movements.setNickname(userInfo.getNickName());
                movements.setTags(StringUtils.split(userInfo.getTags(), ','));
                String publishId = movements.getId();
                long commentCount = this.quanZiApi.queryCommentCount(publishId, CommentTypeEnum.COMMENT.getCode());
                movements.setCommentCount((int) commentCount);
                movements.setDistance("1.2公里"); //TODO 距离
                //如果每次查询列表的时候，都去mongo中查询一遍，性能消耗过大，响应变慢
                //优化策略，每次点赞和喜欢之后，把数据 保存到redis当中,在这个地方 就可以从redis获取了
                //?
                movements.setHasLiked(this.redisTemplate.hasKey(COMMENT_HAS_LIKE+user.getId()+"_"+publishId) ? 1:0); //TODO 是否点赞（1是，0否）
                movements.setHasLoved(this.redisTemplate.hasKey(COMMENT_HAS_LOVE+user.getId()+"_"+publishId) ? 1:0); //TODO 是否喜欢（1是，0否）


                String likeCount = this.redisTemplate.opsForValue().get(COMMENT_LIKE_COUNT+publishId);
                if (StringUtils.isEmpty(likeCount)){
                    movements.setLikeCount(0);
                }else {
                    movements.setLikeCount(Integer.parseInt(likeCount));
                }

//                movements.setLikeCount((int) likeCount); //TODO 点赞数

                String loveCount = this.redisTemplate.opsForValue().get(COMMENT_LOVE_COUNT+publishId);
                if (StringUtils.isEmpty(loveCount)){
                    movements.setLoveCount(0);
                }else {
                    movements.setLoveCount(Integer.parseInt(loveCount));
                }
            }
        }
        pageResult.setItems(movementsList);
        return pageResult;
    }
}
