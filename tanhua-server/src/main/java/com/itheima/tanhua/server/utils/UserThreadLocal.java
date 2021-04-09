package com.itheima.tanhua.server.utils;

import com.itheima.tanhua.sso.pojo.User;

public class UserThreadLocal {

    private final static ThreadLocal<User> LOCAL = new ThreadLocal<>();


    public static void set(User user){
        LOCAL.set(user);
    }

    public static User get(){
        return LOCAL.get();
    }

    public static void remove(){
        LOCAL.remove();
    }
}
