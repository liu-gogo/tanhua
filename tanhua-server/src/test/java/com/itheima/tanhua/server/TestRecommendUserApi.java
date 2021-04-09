package com.itheima.tanhua.server;

import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import com.itheima.tanhua.server.service.RecommendUserService;
import com.itheima.tanhua.server.vo.TodayBest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRecommendUserApi {

    @Autowired
    private RecommendUserService recommendUserService;

    @Test
    public void testQueryTodayBest(){
        RecommendUser recommendUserMaxScore = this.recommendUserService.findRecommendUserMaxScore(1L);
        System.out.println(recommendUserMaxScore);
    }

}