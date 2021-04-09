package com.itheima.tanhua.server.controller;

import com.itheima.tanhua.server.enums.SendMessageTypeEnum;
import com.itheima.tanhua.server.service.CommentsService;
import com.itheima.tanhua.server.service.MovementsService;
import com.itheima.tanhua.server.service.ThreadService;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.PageResult;
import org.apache.http.conn.util.PublicSuffixList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private MovementsService movementsService;

    @Autowired
    private ThreadService threadService;

    @GetMapping
    public ResponseEntity<Object> comments(@RequestParam("movementId") String publishId,
                                           @RequestParam(value = "page",required = false,defaultValue = "1")int page,
                                           @RequestParam(value = "pagesize",required = false,defaultValue = "10")int pageSize){

        try {
            PageResult pageResult = commentsService.comments(publishId,page,pageSize);
            if(pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @PostMapping
    public ResponseEntity<Void> saveContent(@RequestBody Map<String,String> param){
        try {
            String publishId = param.get("movementId");
            String comment = param.get("comment");

            boolean isSuccess = commentsService.saveContent(publishId,comment);
            if (isSuccess){

                threadService.sendMq(UserThreadLocal.get().getId(), SendMessageTypeEnum.COMMENT,publishId);
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping("{id}/like")
    public ResponseEntity<Object> like(@PathVariable("id") String publishId){
        try {
            long like = movementsService.like(publishId);
            return ResponseEntity.ok(like);
        } catch (Exception e) {
            e.printStackTrace();
        }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }


    @GetMapping("{id}/dislike")
    public ResponseEntity<Object> dislike(@PathVariable("id")String publishId){

        try {
            long dislike = movementsService.dislike(publishId);
            return ResponseEntity.ok(dislike);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


    }
}
