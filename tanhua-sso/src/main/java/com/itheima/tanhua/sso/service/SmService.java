package com.itheima.tanhua.sso.service;

import com.aliyuncs.utils.StringUtils;
import com.itheima.tanhua.sso.utils.SendSMS;
import com.itheima.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
public class SmService {

    @Autowired
    private SendSMS sendSMS;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Boolean sendCheckCode(String phone) {
        /**
         * 1. 判断手机号是否合法
         * 2. 调用短信平台发送短信验证码
         * 3. 如果发送成功，存到redis中,有过期时间 和给用户发短信的时间是一致的
         */
        if(StringUtils.isEmpty(phone)){
            return false;
        }

        int i = RandomUtils.nextInt(100000, 999999);

        log.info("验证码是"+i);

        boolean send = true;
        if(send){
            redisTemplate.opsForValue().set("LOGIN_"+phone,String.valueOf(i), Duration.ofMinutes(5));
            return send;
        }
        return false;
    }
}
