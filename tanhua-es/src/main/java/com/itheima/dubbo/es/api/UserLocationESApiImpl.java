package com.itheima.dubbo.es.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dubbo.es.pojo.UserLocationES;

import com.itheima.tanhua.dubbo.server.api.UserLocationApi;
import com.itheima.tanhua.dubbo.server.vo.UserLocationVo;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(version = "1.0.1")
public class UserLocationESApiImpl implements UserLocationApi {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public String updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        /**
         * 1. 先去判断索引是否存在，不存在进行创建
         * 2. 判断地理位置信息 存不存在 存在 更新，不存在插入
         */
        if(!elasticsearchTemplate.indexExists(UserLocationES.class)){
            elasticsearchTemplate.createIndex(UserLocationES.class);
        }
        if (!elasticsearchTemplate.typeExists("tanhua","user_location")){
            elasticsearchTemplate.putMapping(UserLocationES.class);
        }
        GetQuery getQuery = new GetQuery();
        getQuery.setId(userId.toString());
        UserLocationES ul = elasticsearchTemplate.queryForObject(getQuery, UserLocationES.class);
        if (ul == null){
            ul = new UserLocationES();
            ul.setLocation(new GeoPoint(latitude, longitude));
            ul.setAddress(address);
            ul.setUserId(userId);
            ul.setCreated(System.currentTimeMillis());
            ul.setUpdated(ul.getCreated());
            ul.setLastUpdated(ul.getCreated());

            IndexQuery indexQuery = new IndexQueryBuilder().withObject(ul).build();
            this.elasticsearchTemplate.index(indexQuery);
            return ul.getUserId().toString();
        }
        UpdateRequest updateRequest = new UpdateRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("lastUpdated", ul.getUpdated());
        map.put("updated", System.currentTimeMillis());
        map.put("address", address);
        map.put("location", new GeoPoint(latitude, longitude));
        updateRequest.doc(map);

        UpdateQuery updateQuery = new UpdateQueryBuilder().
                withId(userId.toString())
                .withClass(UserLocationES.class)
                .withUpdateRequest(updateRequest)
                .build();
        this.elasticsearchTemplate.update(updateQuery);
        return userId.toString();
    }

    private static final BeanCopier u2uv = BeanCopier.create(UserLocationES.class,UserLocationVo.class,false);
    private UserLocationVo copy(UserLocationES userLocation){
        if(userLocation == null){
            return null;
        }
        UserLocationVo uv = new UserLocationVo();
        u2uv.copy(userLocation,uv,null);
        uv.setLongitude(userLocation.getLocation().getLon());
        uv.setLatitude(userLocation.getLocation().getLat());
        return uv;
    }
    private List<UserLocationVo> copyList(List<UserLocationES> userLocationList){
        List<UserLocationVo> userLocationVoList = new ArrayList<>();
        for (UserLocationES userLocationES : userLocationList) {
            userLocationVoList.add(copy(userLocationES));
        }
        return userLocationVoList;
    }

    @Override
    public UserLocationVo queryUserLocation(Long userId) {

        GetQuery getQuery = new GetQuery();
        getQuery.setId(userId.toString());
        UserLocationES ul = elasticsearchTemplate.queryForObject(getQuery, UserLocationES.class);

        return copy(ul);
    }

    @Override
    public List<UserLocationVo> queryUserLocationList(Double longitude, Double latitude, long range) {
        //log.info("es的附近的人查询...");
        //withPageable 分页
        PageRequest pageRequest = PageRequest.of(0,10);
        //withSort  排序 距离排序
        GeoPoint point = new GeoPoint(latitude,longitude);
        //
        String fieldName = "location";
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder(fieldName, point);

        //withQuery 距离查询
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder(fieldName);
        geoDistanceQueryBuilder.point(latitude,longitude);
        geoDistanceQueryBuilder.distance(Double.parseDouble(String.valueOf(range))/1000, DistanceUnit.KILOMETERS);

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //代表此查询条件 必须满足
        boolQueryBuilder.must(geoDistanceQueryBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageRequest)
                .withSort(geoDistanceSortBuilder)
                .withQuery(boolQueryBuilder)
                .build();
        List<UserLocationES> userLocationES = this.elasticsearchTemplate.queryForList(searchQuery, UserLocationES.class);

        return copyList(userLocationES);
    }
}
