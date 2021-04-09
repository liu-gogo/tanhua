package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.api.VideoApi;
import com.itheima.tanhua.dubbo.server.pojo.Video;
import com.itheima.tanhua.dubbo.server.vo.CommentTypeEnum;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.PicUploadResult;
import com.itheima.tanhua.server.vo.VideoVo;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoService {
    @Reference(version = "1.0.0")
    private VideoApi videoApi;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private SSOService ssoService;
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private MovementsService movementsService;

    @Autowired
    private CommentsService commentsService;



    @Value("${fdfs.web-server-url}")
    private String fastUrl;


    public boolean saveVideo(MultipartFile videoThumbnail, MultipartFile videoFile) {

            User user = UserThreadLocal.get();
            Video video = new Video();

            video.setUserId(user.getId());
            video.setSeeType(1);
            video.setText("好家伙");
            PicUploadResult upload = picUploadService.upload(videoThumbnail);

            if (upload.isSuccess()) {
                video.setPicUrl(upload.getImageUrl());

                try {
                    StorePath storePath = fastFileStorageClient.uploadFile(videoFile.getInputStream(), videoFile.getSize(), StringUtils.substringAfterLast(videoFile.getOriginalFilename(), "."), null);
                    video.setVideoUrl(fastUrl + storePath.getFullPath());

                    return videoApi.saveVideo(video);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        return false;
    }


    public PageResult queryVideoList(Integer pageNum, Integer pageSize) {
        User user = UserThreadLocal.get();

        List<Video> videosList = videoApi.queryVideoList(pageNum, pageSize);

        PageResult pageResult = new PageResult();
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(0);
        pageResult.setCounts(0);

        if (CollectionUtils.isEmpty(videosList)) {
            return pageResult;
        }
        List<VideoVo> videoVoList = new ArrayList<>();

        List<Long> userIdList = new ArrayList<>();

        for (Video video : videosList) {
            if (!userIdList.contains(video.getUserId())) {
                userIdList.add(video.getUserId());
            }
            VideoVo videoVo = new VideoVo();
            videoVo.setId(video.getId().toHexString());
            videoVo.setCover(video.getPicUrl());
            videoVo.setVideoUrl(video.getVideoUrl());
            videoVo.setUserId(video.getUserId());
            videoVo.setSignature("美丽小视频");

            videoVoList.add(videoVo);
        }

        List<UserInfo> userInfoList = ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(), userInfo);
        }

        for (VideoVo videoVo : videoVoList) {
            UserInfo userInfo = map.get(videoVo.getUserId());
            if (userInfo != null){
                videoVo.setAvatar(userInfo.getLogo());
                videoVo.setNickname(userInfo.getNickName());
            }

            long commentCount = quanZiApi.queryCommentCount(videoVo.getId(), CommentTypeEnum.COMMENT.getCode());
            videoVo.setCommentCount((int) commentCount);

            videoVo.setHasLiked(redisTemplate.hasKey(MovementsService.COMMENT_HAS_LIKE+videoVo.getUserId()+"_"+videoVo.getId())?1:0);

            String likeCount = redisTemplate.opsForValue().get(MovementsService.COMMENT_LIKE_COUNT + videoVo.getId());
            if (StringUtils.isEmpty(likeCount)){
                videoVo.setLikeCount(0);
            }else {
                videoVo.setLikeCount(Integer.valueOf(likeCount));
            }

            videoVo.setHasFocus(redisTemplate.hasKey("USER_FOCUS_"+user.getId()+"_"+videoVo.getUserId())? 1:0);

        }
        pageResult.setItems(videoVoList);
        return pageResult;
    }


    public Long like(String videoId) {

        return  movementsService.like(videoId);
    }

    public Long dislike(String videoId) {

        return movementsService.dislike(videoId);
    }


    public PageResult queryCommentList(String videoId, Integer pageNum, Integer pageSzie) {
        return commentsService.comments(videoId, pageNum, pageSzie);
    }

    public boolean saveComment(String videoId,String comment) {


        return commentsService.saveContent(videoId, comment);
    }


    public boolean userFocus(Long followUserId) {
        User user = UserThreadLocal.get();
        Long userId = user.getId();

        boolean isSuccess = videoApi.saveFollowUser(userId, followUserId);


        if (isSuccess){
            redisTemplate.opsForValue().set("USER_FOCUS_"+userId+"_"+followUserId,"1");
        }
        return true;
    }

    public boolean userUnFocus(Long followUserId) {
        User user = UserThreadLocal.get();
        Long userId = user.getId();

        boolean isSuccess = videoApi.removeFollowUser(userId, followUserId);
        if (isSuccess){
            redisTemplate.delete("USER_FOCUS_"+userId+"_"+followUserId);
        }

        return true;
    }
}