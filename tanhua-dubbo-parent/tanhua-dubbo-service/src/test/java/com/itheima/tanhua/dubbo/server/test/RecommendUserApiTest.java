package com.itheima.tanhua.dubbo.server.test;


import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.api.RecommendUserApi;
import com.itheima.tanhua.dubbo.server.pojo.Publish;
import com.itheima.tanhua.dubbo.server.pojo.TimeLine;
import com.itheima.tanhua.dubbo.server.DubboApplication;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@SpringBootTest(classes = DubboApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RecommendUserApiTest {


    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;


    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private MongoTemplate mongoTemplate;




    @Test
    public void testRecommendPublish(){
        //查询用户id为2的动态作为推荐动态的数据
        List<Publish> pageInfo = this.quanZiApi.queryPublishList(1L, 1, 10,false);
        for (Publish record : pageInfo) {

            TimeLine timeLine = new TimeLine();
            timeLine.setId(ObjectId.get());
            timeLine.setPublishId(record.getId());
            timeLine.setUserId(record.getUserId());
            timeLine.setDate(System.currentTimeMillis());

            this.mongoTemplate.save(timeLine, "quanzi_time_line_recommend");
        }
    }
}
