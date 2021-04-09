package com.itheima.tanhua.sso.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.sso.dto.UserInfoDto;
import com.itheima.tanhua.sso.enums.SexEnum;
import com.itheima.tanhua.sso.mapper.UserInfoMapper;
import com.itheima.tanhua.sso.mapper.UserMapper;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import com.itheima.tanhua.sso.utils.JWTUtils;
import com.itheima.tanhua.sso.vo.LoginResult;
import com.itheima.tanhua.sso.vo.PicUploadResult;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class UserService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private FaceEngineService faceEngineService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private HuanXinService huanXinService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public LoginResult loginVerification(String phone, String code) {
        /**
         * 1. 验证码校验
         * 2. 根据手机号 去数据库查询用户是否存在
         * 3. 如果不存在 注册用户 userId
         * 4. 返回userId
         * 5. 生成token 返回
         *     1. token 可以设置过期时间，可以根据token的过期时间来去判断用户的登录有效时长
         *     2. token 不设置过期时长，但是存储在redis当中 利用redis的过期来做
         * 6. 发送登录的信息 到rocketmq队列中，等待别的程序去处理 根登录逻辑就解耦了
         */

        if(StringUtils.isEmpty(code)){
            log.info("验证码未传递");
            return null;

        }

        String redisCode = redisTemplate.opsForValue().get("LOGIN_" + phone);

        if(StringUtils.isEmpty(redisCode)){
            log.info("验证码已过期");
            return null;
        }
        System.out.println(redisCode);

        System.out.println(code);
        if(!code.equals(redisCode)){
            log.info("验证码不正确");
            return null;
        }

        boolean isNew = false;
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("mobile", phone);

        User user = userMapper.selectOne(queryWrapper);
        if(user == null){
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));
            userMapper.insert(user);
            isNew = true;
            huanXinService.register(user.getId());

        }

        Long userId = user.getId();

        Map<String,Object> map = new HashMap<>();
        map.put("userId", userId);
        String token = JWTUtils.token(map, jwtSecret);

        try {
            redisTemplate.opsForValue().set(token, MAPPER.writeValueAsString(user), Duration.ofDays(7));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, Object> queueMap = new HashMap<>();

        queueMap.put("userId", user.getId());
        queueMap.put("created",System.currentTimeMillis());
        queueMap.put("isNew", isNew);
        queueMap.put("mobile", phone);

        rocketMQTemplate.convertAndSend("sso-login",queueMap);


        LoginResult loginResult = new LoginResult();
        loginResult.setIsNew(isNew);
        loginResult.setToken(token);
        return loginResult;
    }

    public User checkToken(String token){
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

            String redisToken = redisTemplate.opsForValue().get(token);
            if(StringUtils.isEmpty(redisToken)){
                return null;
            }

            User user = MAPPER.readValue(redisToken, User.class);
            return user;

        } catch (ExpiredJwtException e) {
            System.out.println("token已经过期");
        }catch (Exception e){
            System.out.println("token不合法");
        }

        return null;
    }

    public boolean loginReginfo(UserInfoDto userInfoDto, String token) {
        /**
         * 1. 检测token是否合法，如果合法返回user对象
         * 2. 拿上userId 去userInfo表 保存数据(先去查询userInfo表是否有数据)
         */
        User user = checkToken(token);
        if (user == null){
            return false;
        }

        Long id = user.getId();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",id);
        queryWrapper.last("limit 1");
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (userInfo == null){
            userInfo = new UserInfo();
            userInfo.setUserId(user.getId());
            userInfo.setSex(StringUtils.equalsIgnoreCase(userInfoDto.getGender(), "man") ? SexEnum.MAN : SexEnum.WOMAN);
            userInfo.setNickName(userInfoDto.getNickname());
            userInfo.setBirthday(userInfoDto.getBirthday());
            userInfo.setCity(userInfoDto.getCity());
            userInfoMapper.insert(userInfo);
            return true;
        }

        return false;
    }

    public boolean savaLogo(MultipartFile multipartFile,String token){
        /**
         * 1. 检查token是否合法
         * 2. 检测图片是否为人像
         * 3. 如果为人像 ，进行oss上传
         * 4. 上传成功，进行userinfo表的更新
         */

        User user = checkToken(token);
        if(user == null){
            return false;
        }

        try {
            boolean isPortrait = faceEngineService.checkIsPortrait(multipartFile.getBytes());
            if(!isPortrait){
                return false;
            }

            PicUploadResult isResult = picUploadService.upload(multipartFile);
            if(!isResult.isSuccess()){
                return false;
            }

            Long userId = user.getId();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", userId);
            queryWrapper.last("limit 1");
            UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
            if(!(userInfo == null)){
                userInfo.setLogo(isResult.getImagePath());
                userInfoMapper.updateById(userInfo);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }



}
