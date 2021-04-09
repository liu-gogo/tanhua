package com.itheima.tanhua.sso.api;

import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;

import java.util.List;

public interface SSOAPI {
    User checkToken(String token);

    UserInfo findUserInfo(long userId);

    List<UserInfo> findUserInfoList(List<Long> userIdList, Integer age, String city, String education, String gender);

    boolean contactUsers(Long userId, Long friendId);

    List<UserInfo> findUserInfoList(List<Long> userIdList, String keyword);

    boolean sendMsg(Long userId,String type,String msg);

    Boolean updateUserInfo(UserInfo userInfo);
}
