package com.itheima.tanhua.server.service;

import com.itheima.tanhua.server.enums.SendMessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ThreadService {
    @Autowired
    private QuanziMQService quanziMQService;

    @Async("asyncServiceExecutor")
    public void sendMq(Long userId, SendMessageTypeEnum typeEnum, String publishId){
        quanziMQService.sendMsg(userId, typeEnum, publishId);
    }
}
