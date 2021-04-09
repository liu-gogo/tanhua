package com.itheima.tanhua.sso.service;

import com.itheima.tanhua.sso.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ThreadService {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Async("asyncServiceExecutor")
    public void sendMQ(User user) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", user.getId());
            msg.put("date", System.currentTimeMillis());
            rocketMQTemplate.convertAndSend("itcast-tanhua-login",msg);
            log.info("mq send start.......");
            Thread.sleep(5000);
            log.info("mq send success。。。。。");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}