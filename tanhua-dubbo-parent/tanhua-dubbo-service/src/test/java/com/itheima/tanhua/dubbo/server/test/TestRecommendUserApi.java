package com.itheima.tanhua.dubbo.server.test;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.tanhua.dubbo.server.api.RecommendUserApi;


import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestRecommendUserApi {

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testQueryWithMaxScore(){
        RecommendUser x = this.recommendUserApi.queryMaxScore(95L);
        System.out.println(x);
        System.out.println(this.recommendUserApi.queryMaxScore(98L));
        System.out.println(this.recommendUserApi.queryMaxScore(37L));

        restTemplate.getForObject("sss", RecommendUser.class);
    }

    @Test
    public void testQueryPageInfo(){
        System.out.println(this.recommendUserApi.queryRecommendUserList(1L,1,5));
        System.out.println(this.recommendUserApi.queryRecommendUserList(1L,2,5));
        System.out.println(this.recommendUserApi.queryRecommendUserList(1L,3,5));
    }

}