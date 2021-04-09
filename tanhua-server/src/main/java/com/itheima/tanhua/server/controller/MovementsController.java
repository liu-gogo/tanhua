package com.itheima.tanhua.server.controller;


import com.itheima.tanhua.server.enums.SendMessageTypeEnum;
import com.itheima.tanhua.server.service.MovementsService;
import com.itheima.tanhua.server.service.ThreadService;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.Movements;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.VisitorsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("movements")
public class MovementsController {

    @Autowired
    private MovementsService movementsService;


    @Autowired
    private ThreadService threadService;

    //发布
    //POST  /movements



    @PostMapping
    public ResponseEntity<Object> savePublish(@RequestParam(value = "textContent", required = false) String textContent,
                                              @RequestParam(value = "location", required = false) String location,
                                              @RequestParam(value = "latitude", required = false) String latitude,
                                              @RequestParam(value = "longitude", required = false) String longitude,
                                              @RequestParam(value = "imageContent", required = false) MultipartFile[] multipartFile
                                              ){
        String publishId = this.movementsService.savePublish(textContent,location,latitude,longitude,multipartFile);

        if (publishId != null){
            threadService.sendMq(UserThreadLocal.get().getId(), SendMessageTypeEnum.PUBLISH,publishId );
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping
    public ResponseEntity<Object> queryPublishList(@RequestParam(value = "page",required = false,defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pagesize",required = false,defaultValue = "10") int pageSize){

        Boolean isRecommend = false;

        PageResult pageResult = movementsService.queryPublishList(pageNum,pageSize,isRecommend);

        if(pageResult != null){
            return ResponseEntity.ok(pageResult);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


    }

    @GetMapping("recommend")
    public ResponseEntity<Object> recommend(@RequestParam(value = "page",required = false,defaultValue = "1") int page,
                                            @RequestParam(value = "pagesize",required = false,defaultValue = "10") int pageSize){
        //pageResult items->movements
        PageResult pageResult = this.movementsService.queryPublishList(page,pageSize,true);

        if (pageResult != null){
            return ResponseEntity.ok(pageResult);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping("{id}/love")
    public ResponseEntity<Object> love(@PathVariable("id")String publishId){


        try {
            long loveCount = this.movementsService.love(publishId);
            threadService.sendMq(UserThreadLocal.get().getId(),SendMessageTypeEnum.LOVE,publishId);
            return ResponseEntity.ok(loveCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //动态-点赞
    //GET /movements/:id/like
    @GetMapping("{id}/like")
    public ResponseEntity<Object> like(@PathVariable("id") String publishId){
        try {
            long likeCount = this.movementsService.like(publishId);
            threadService.sendMq(UserThreadLocal.get().getId(),SendMessageTypeEnum.LIKE,publishId);
            return ResponseEntity.ok(likeCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //动态-取消点赞
    //GET /movements/:id/dislike

    @GetMapping("{id}/dislike")
    public ResponseEntity<Object> dislike(@PathVariable("id") String publishId){
        try {
            long likeCount = this.movementsService.dislike(publishId);
            threadService.sendMq(UserThreadLocal.get().getId(),SendMessageTypeEnum.CANCEL_LIKE,publishId);
            return ResponseEntity.ok(likeCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //动态-取消喜欢
    //GET /movements/:id/unlove

    @GetMapping("{id}/unlove")
    public ResponseEntity<Object> unlove(@PathVariable("id") String publishId){
        try {
            long loveCount = this.movementsService.unlove(publishId);

            threadService.sendMq(UserThreadLocal.get().getId(),SendMessageTypeEnum.CANCEL_LOVE,publishId);
            return ResponseEntity.ok(loveCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //单条动态
    // GET /movements/:id

    @GetMapping("{id}")
    public ResponseEntity<Object> queryPublish(@PathVariable("id") String publishId){
        try {
            Movements movements = this.movementsService.queryPublish(publishId);
            threadService.sendMq(UserThreadLocal.get().getId(),SendMessageTypeEnum.WATCH_PUBLISH,publishId);
            if (movements != null){
                return ResponseEntity.ok(movements);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping("visitors")
    public ResponseEntity<Object> queryVisitors(){
        try {
            List<VisitorsVo> visitorsVoList = movementsService.queryVisitors();
            if (visitorsVoList != null){
                return ResponseEntity.ok(visitorsVoList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("all")
    public ResponseEntity<Object> all(@RequestParam(value = "page",required = false,defaultValue = "1") int page,
                                      @RequestParam(value = "pagesize",required = false,defaultValue = "10") int pageSize,
                                      @RequestParam(value = "userId") Long userId){
        try {
            PageResult pageResult = this.movementsService.all(page,pageSize,userId);
            if (pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

