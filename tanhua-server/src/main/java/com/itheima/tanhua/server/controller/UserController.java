package com.itheima.tanhua.server.controller;


import com.itheima.tanhua.server.service.UserService;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("users")
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    //用户资料读取
    // GET/users

    @GetMapping
    public ResponseEntity<Object> queryUserInfo(@RequestParam(value = "userID",required = false) Long userId,
                                                @RequestParam(value = "huanxinID",required = false) Long huanxinID){
        try {
            UserInfoVo userInfoVo = userService.queryUserInfo(userId,huanxinID);
            if (userInfoVo != null){
                return ResponseEntity.ok(userInfoVo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 更新用户信息
     * 接口路径：PUT /users
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserInfoVo userInfoVo){
        try {
            Boolean bool = this.userService.updateUserInfo(userInfoVo);
            if(bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    //互相喜欢，喜欢，粉丝 - 统计
    //GET/users/counts

    @GetMapping("counts")
    public ResponseEntity<Object> counts(){
        try {
            Map<String,Long> result = this.userService.counts();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
    //GET/users/friends/:type

    @GetMapping("friends/{type}")
    public ResponseEntity<Object> friends(@PathVariable("type") int type,
                                          @RequestParam("page") int page,
                                          @RequestParam("pagesize") int pageSize,
                                          @RequestParam(value = "nickname",required = false) String nickname){
        try {
            PageResult pageResult = this.userService.friends(type,page,pageSize,nickname);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
