package com.itheima.tanhua.server.controller;

import com.itheima.tanhua.server.service.CommentsService;
import com.itheima.tanhua.server.service.VideoService;
import com.itheima.tanhua.server.vo.PageResult;
import org.apache.http.protocol.HTTP;
import org.apache.ibatis.annotations.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("smallVideos")
public class VideoController {
    @Autowired
    private VideoService videoService;



    @PostMapping
    public ResponseEntity<Object> saveVideo(@RequestParam(value = "videoThumbnail") MultipartFile videoThumbnail,
                                            @RequestParam(value = "videoFile") MultipartFile videoFile){

        try {
            boolean isSuccess = videoService.saveVideo(videoThumbnail,videoFile);

            if(isSuccess){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping
    public ResponseEntity<Object> queryVideoList(@RequestParam(value = "page",required = false,defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pagesize",required = false,defaultValue = "10") Integer pageSize){

        try {
            PageResult pageResult =  videoService.queryVideoList(pageNum,pageSize);

            if (pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("{id}/like")
    public ResponseEntity<Long> like(@PathVariable("id") String videoId){

        try {
            Long isSuccess =  videoService.like(videoId);

            return  ResponseEntity.ok(isSuccess);
        } catch (Exception e) {
            e.printStackTrace();
        }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("{id}/dislike")

    public ResponseEntity<Long> disLike(@PathVariable("id") String videoId){
        try {
            Long isSuccess =  videoService.dislike(videoId);

            return  ResponseEntity.ok(isSuccess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/comments")
    public ResponseEntity<PageResult> queryCommentList(@PathVariable("id") String videoId,
                                                       @RequestParam(value = "page",defaultValue = "1",required = false) Integer pageNum,
                                                       @RequestParam(value = "pagesize",defaultValue = "1",required = false) Integer pageSzie){

       PageResult pageResult =  videoService.queryCommentList(videoId,pageNum,pageSzie);

       if(pageResult != null){
           return ResponseEntity.ok(pageResult);
       }

       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("{id}/comments")
    public ResponseEntity<Object> saveComment(@PathVariable("id")String videoId,
                                            @RequestBody Map<String, String> param){
        String comment = param.get("comment");

        boolean isSuccess = videoService.saveComment(videoId,comment);

        if (isSuccess){
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }


    /**
     * 评论点赞
     *
     * @param publishId
     * @return
     */
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Object> commentsLikeComment(@PathVariable("id") String publishId) {
        try {
            Long likeComment =videoService.like(publishId);

            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 评论取消点赞
     *
     * @param publishId
     * @return
     */
    @PostMapping("/comments/{id}/dislike")
    public ResponseEntity<Object> disCommentsLikeComment(@PathVariable("id") String publishId) {

        try {
            Long likeComment =videoService.dislike(publishId);

            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @PostMapping("{uid}/userFocus")
    public ResponseEntity<Object> userFocus(@PathVariable("id") Long followUserId){

    boolean isSuccess = videoService.userFocus(followUserId);

    if (isSuccess){
        return ResponseEntity.ok(null);
    }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("{uid}/userUnFocus")
    public ResponseEntity<Object> userUnFocus(@PathVariable("id") Long followUserId){

        boolean isSuccess = videoService.userUnFocus(followUserId);

        if (isSuccess){
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}



