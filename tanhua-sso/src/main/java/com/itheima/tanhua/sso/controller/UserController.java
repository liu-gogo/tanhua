package com.itheima.tanhua.sso.controller;

import com.itheima.tanhua.sso.dto.UserInfoDto;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.service.SmService;
import com.itheima.tanhua.sso.service.UserService;
import com.itheima.tanhua.sso.vo.ErrorResult;
import com.itheima.tanhua.sso.vo.LoginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.REException;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("user")
public class UserController {
    @Autowired
    private SmService smsService;

    @Autowired
    private UserService userService;

    @PostMapping("login")
    public ResponseEntity<ErrorResult> sendCheckCode(@RequestBody Map<String,String> params){

        String phone = params.get("phone");
        Boolean isSuccess = smsService.sendCheckCode(phone);
        if(isSuccess){
            return ResponseEntity.ok(null);
        }
        ErrorResult errorResult = ErrorResult.builder().
                errCode("000000").
                errMessage("发送验证码失败").
                build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);

    }

    @PostMapping("loginVerification")
    public ResponseEntity<Object> loginVerification(@RequestBody Map<String, String> params){
        String phone = params.get("phone");
        String code = params.get("verificationCode");

        LoginResult loginResult = userService.loginVerification(phone,code);

        if(loginResult != null){
            return ResponseEntity.ok(loginResult);
        }

        ErrorResult errorResult = ErrorResult.builder().errCode("000002").errMessage("登录失败").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }

    @PostMapping("loginReginfo")
    public ResponseEntity<Object> loginReginfo(@RequestBody UserInfoDto userInfoDto, @RequestHeader("Authorization")String token){
        boolean isSave = userService.loginReginfo(userInfoDto, token);

        if(isSave){
            return ResponseEntity.ok(null);
        }

        ErrorResult errorResult = ErrorResult.builder()
                .errCode("000002")
                .errMessage("保存失败")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);

    }

    @PostMapping("loginReginfo/head")
    public ResponseEntity<Object> savaLogo(@RequestParam("headPhoto") MultipartFile multipartFile,
                                           @RequestHeader("Authorization") String token){

        boolean b = userService.savaLogo(multipartFile, token);
        if(b){
           return ResponseEntity.ok(null);
        }

        ErrorResult errorResult = ErrorResult.builder()
                .errCode("000002")
                .errMessage("保存失败")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);

    }
}
