package com.itheima.tanhua.dubbo.server.api.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.itheima.tanhua.dubbo.server.api.UserLocationApi;
import com.itheima.tanhua.dubbo.server.pojo.UserLocation;
import com.itheima.tanhua.dubbo.server.vo.UserLocationVo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service(version = "1.0.0")
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final BeanCopier u2uv = BeanCopier.create(UserLocation.class, UserLocationVo.class,false);

    @Override
    public String updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        /**
         * 查询此用户是否已经有地理位置信息了
         * 有 更新 无 新增
         */
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation userLocation = this.mongoTemplate.findOne(query, UserLocation.class);
        if (userLocation == null){
            //insert
            //新增
            userLocation = new UserLocation();
            userLocation.setAddress(address);
            userLocation.setLocation(new GeoJsonPoint(longitude, latitude));
            userLocation.setUserId(userId);
            userLocation.setId(ObjectId.get());
            userLocation.setCreated(System.currentTimeMillis());
            userLocation.setUpdated(userLocation.getCreated());
            userLocation.setLastUpdated(userLocation.getCreated());

            this.mongoTemplate.save(userLocation);
            return userLocation.getId().toHexString();
        }
        userLocation.setAddress(address);
        userLocation.setLocation(new GeoJsonPoint(longitude, latitude));
        userLocation.setLastUpdated(userLocation.getUpdated());
        userLocation.setUpdated(System.currentTimeMillis());

        Update update = Update
                .update("location", userLocation.getLocation())
                .set("updated", userLocation.getUpdated())
                .set("lastUpdated", userLocation.getLastUpdated())
                .set("address",userLocation.getAddress());
        this.mongoTemplate.updateFirst(query,update,UserLocation.class);
        return userLocation.getId().toHexString();
    }

    private UserLocationVo copy(UserLocation userLocation){
        if(userLocation == null){
            return null;
        }
        UserLocationVo uv = new UserLocationVo();
        u2uv.copy(userLocation,uv,null);
        uv.setLongitude(userLocation.getLocation().getX());
        uv.setLatitude(userLocation.getLocation().getY());
        return uv;
    }

    @Override
    public UserLocationVo queryUserLocation(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation userLocation = this.mongoTemplate.findOne(query, UserLocation.class);
        return copy(userLocation);
    }

    @Override
    public List<UserLocationVo> queryUserLocationList(Double longitude, Double latitude, long range) {
        //中心点
        GeoJsonPoint center = new GeoJsonPoint(longitude,latitude);
        //半径  range m
        Distance radius = new Distance(range, Metrics.MILES);
        //圆 圈定范围
        Circle circle = new Circle(center,radius);

        Query query = Query.query(Criteria.where("location").withinSphere(circle));

        return UserLocationVo.formatToList(this.mongoTemplate.find(query,UserLocation.class));
    }
}
