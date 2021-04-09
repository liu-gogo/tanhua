package com.itheima.tanhua.sso.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.sso.config.AliyunConfig;
import com.itheima.tanhua.sso.pojo.User;


import com.itheima.tanhua.sso.pojo.UserInfo;
import com.itheima.tanhua.sso.service.HuanXinService;
import com.itheima.tanhua.sso.service.UserInfoService;
import com.itheima.tanhua.sso.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service(version = "1.0.0")
public class SSOAPIimpl implements SSOAPI{
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private HuanXinService huanXinService;



    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
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

    @Override
    public UserInfo findUserInfo(long userId) {
        return userInfoService.findUserInfoByUserId(userId);
    }

    @Override
    public List<UserInfo> findUserInfoList(List<Long> userIdList, Integer age, String city, String education, String gender) {
        List<UserInfo> userInfoList = userInfoService.findUserInfoList(userIdList,age,city,education,gender);

        for (UserInfo userInfo : userInfoList) {

            userInfo.fillLogo(aliyunConfig.getUrlPrefix());
        }
        return userInfoList;
    }

    @Override
    public boolean contactUsers(Long userId, Long friendId) {
        return huanXinService.contactUsers(userId, friendId);
    }

    @Override
    public List<UserInfo> findUserInfoList(List<Long> userIdList, String keyword) {
        return userInfoService.findUserInfoList(userIdList, keyword);
    }

    @Override
    public boolean sendMsg(Long userId, String type, String msg) {
        return huanXinService.sendMsg(userId, type, msg);
    }

    @Override
    public Boolean updateUserInfo(UserInfo userInfo) {

        return userInfoService.updateUserInfo(userInfo);
    }


}
