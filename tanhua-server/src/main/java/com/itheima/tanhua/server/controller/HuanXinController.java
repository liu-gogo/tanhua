package com.itheima.tanhua.server.controller;


import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.HuanXinUser;
import com.itheima.tanhua.sso.pojo.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("huanxin")
public class HuanXinController {

    @GetMapping("user")
    public ResponseEntity<HuanXinUser> queryHuanXinUser(){
        User user = UserThreadLocal.get();

        HuanXinUser huanXinUser = new HuanXinUser();
        huanXinUser.setUsername(user.getId().toString());
        huanXinUser.setPassword(DigestUtils.md5Hex(user.getId() + "_itcast_tanhua"));

        return ResponseEntity.ok(huanXinUser);
    }


}
