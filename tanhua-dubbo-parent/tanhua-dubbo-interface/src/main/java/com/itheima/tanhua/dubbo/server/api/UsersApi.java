package com.itheima.tanhua.dubbo.server.api;

import com.itheima.tanhua.dubbo.server.pojo.Users;
import com.itheima.tanhua.sso.pojo.User;

import java.util.List;

public interface UsersApi {

    String saveUser(Users users);

    List<Users> findAllUsersList(Long userId);

    List<Users> findUsersListPage(Long userId, int page, int pageSize);
}
