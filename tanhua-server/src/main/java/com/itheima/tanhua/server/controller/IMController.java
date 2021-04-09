package com.itheima.tanhua.server.controller;

import com.itheima.tanhua.server.pojo.Announcement;
import com.itheima.tanhua.server.service.ImService;
import com.itheima.tanhua.server.utils.NoLogin;
import com.itheima.tanhua.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("messages")
public class IMController {

    @Autowired
    private ImService imService;

    @PostMapping("contacts")
    public ResponseEntity<Object> addContacts(@RequestBody Map<String, Long> params){
        Long firendId = params.get("userId");

        try {
            boolean isSave = imService.addContacts(firendId);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping("contacts")
    public ResponseEntity<Object> contactsList(@RequestParam(value = "page",defaultValue = "1",required = false) int page,
                                               @RequestParam(value = "pagesize",defaultValue = "10",required = false) int pageSize,
                                               @RequestParam(value = "keyword",required = false) String keyword){
        try {
            PageResult pageResult = this.imService.contactsList(page,pageSize,keyword);
            if (pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    @GetMapping("likes")
    public ResponseEntity<Object> likes(@RequestParam(value = "page",defaultValue = "1",required = false) int page,
                                        @RequestParam(value = "pagesize",defaultValue = "10",required = false) int pageSize
                                         ){
        try {
            PageResult pageResult = this.imService.likes(page,pageSize);
            if (pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }


    @GetMapping("loves")
    public ResponseEntity <Object> loves(@RequestParam(value = "page",defaultValue = "1",required = false) int page,
                                        @RequestParam(value = "pagesize",defaultValue = "10",required = false) int pageSize
    ){
        try {
            PageResult pageResult = this.imService.loves(page,pageSize);
            if (pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    @GetMapping("comments")
    public ResponseEntity<Object> comments(@RequestParam(value = "page",defaultValue = "1",required = false) int page,
                                        @RequestParam(value = "pagesize",defaultValue = "10",required = false) int pageSize
    ){
        try {
            PageResult pageResult = this.imService.comments(page,pageSize);
            if (pageResult != null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }


    @GetMapping("announcements")
    @NoLogin
    public ResponseEntity<PageResult> queryMessageAnnouncementList(@RequestParam(value = "page",defaultValue = "1",required = false) int page,
                                                                   @RequestParam(value = "pagesize",defaultValue = "10",required = false) int pageSize){

        PageResult<Announcement> pageResult = imService.queryMessageAnnouncementList(page,pageSize);

        return ResponseEntity.ok(pageResult);

    }
}
