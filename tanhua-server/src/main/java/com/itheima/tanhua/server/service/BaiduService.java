package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;

import com.itheima.tanhua.dubbo.server.api.UserLocationApi;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.sso.pojo.User;
import org.springframework.stereotype.Service;

@Service
public class BaiduService {

    // @Reference(version = "1.0.0") mongo
    //    private UserLocationApi userLocationApi;
    @Reference(version = "1.0.1") //es
    private UserLocationApi userLocationApi;

    public Boolean updateLocation(Double longitude, Double latitude, String address) {
        try {
            User user = UserThreadLocal.get();
            this.userLocationApi.updateUserLocation(user.getId(), longitude, latitude, address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}