package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;

import com.itheima.tanhua.sso.api.SSOAPI;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class SSOService {

    @Reference(version = "1.0.0")
    private SSOAPI ssoapi;


    public User checkToken(String token) {
        return ssoapi.checkToken(token);
    }


    public UserInfo findUserInfoByUserId(Long userId) {
        return ssoapi.findUserInfo(userId);
    }

    public List<UserInfo> findUserInfoList(List<Long> userIdList,
                                           Integer age,
                                           String city,
                                           String education,
                                           String gender) {

        return ssoapi.findUserInfoList(userIdList,age,city,education,gender);
    }

    public List<UserInfo> findUserInfoList(List<Long> userIdList) {

        return findUserInfoList(userIdList,null,null,null,null);
    }

    public boolean saveContacts(Long userId, Long firendId) {
        return ssoapi.contactUsers(userId, firendId);
    }

    public List<UserInfo> findUserInfoList(List<Long> userIdList, String keyword) {

        return ssoapi.findUserInfoList(userIdList,keyword);
    }

    public List<UserInfo> findUserInfoListByGender(List<Long> userIdList,
                                                   String gender) {

        return ssoapi.findUserInfoList(userIdList,null,null,null,gender);
    }

    public boolean sendMsg(Long userId, String txt, String reply) {
        return ssoapi.sendMsg(userId, txt, reply);

    }

    public Boolean updateUserInfo(UserInfo userInfo) {

        return ssoapi.updateUserInfo(userInfo);
    }
}
