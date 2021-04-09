package com.itheima.tanhua.dubbo.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IDService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Long created(String type, String strId) {
        String idHashKey = "TANHUA_ID_HASH_"+type;

        if(redisTemplate.opsForHash().hasKey(idHashKey,strId)){
            return Long.valueOf(redisTemplate.opsForHash().get(idHashKey,strId).toString());
        }

        String  idKey = "TANHUA_ID_" + type;

        Long id = redisTemplate.opsForValue().increment(idHashKey);

        redisTemplate.opsForHash().put(idKey, strId, id.toString());
        return id;
    }
}
