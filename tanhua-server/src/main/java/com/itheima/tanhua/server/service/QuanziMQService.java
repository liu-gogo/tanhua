package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.pojo.Publish;
import com.itheima.tanhua.server.enums.SendMessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QuanziMQService {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public Boolean sendMsg(Long userId, SendMessageTypeEnum typeEnum, String publishId){

        try {
            Publish publish = quanZiApi.queryPublish(publishId);

            Map<String, Object> msg = new HashMap<>();
            msg.put("publish", publish);
            msg.put("date", System.currentTimeMillis());
            msg.put("userId", userId);
            msg.put("pid", publish.getPid());
            msg.put("type", typeEnum.getValue());

            rocketMQTemplate.convertAndSend("tanhua-quanzi",msg);

        } catch (Exception e) {
            log.error("发送消息失败!publishId = {} ,type = {}", publishId,typeEnum.getValue(),e);

            return false;
        }
        return true;
    }
}
