package com.itheima.tanhua.dubbo.server.api;



import com.itheima.tanhua.dubbo.server.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {

    /**
     * 更新用户地理位置
     *
     * @return
     */
    String updateUserLocation(Long userId, Double longitude, Double latitude, String address);

    /**
     * 获取用户对应的地理位置信息
     * @param userId
     * @return
     */
    UserLocationVo queryUserLocation(Long userId);

    /**
     * 根据经纬度 查询 range范围内的用户地理位置
     * @param longitude
     * @param latitude
     * @param range  单位m
     * @return
     */
    List<UserLocationVo> queryUserLocationList(Double longitude,Double latitude,long range);
}
