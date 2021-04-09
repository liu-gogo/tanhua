package com.itheima.tanhua.server.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.server.utils.Cache;
import com.itheima.tanhua.server.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {


        return methodParameter.hasMethodAnnotation(GetMapping.class) && methodParameter.hasMethodAnnotation(Cache.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (body == null){
            return null;
        }

        try {
            String redisValue = mapper.writeValueAsString(body);
            String redisKey = CacheUtils.createRedisKey(((ServletServerHttpRequest) serverHttpRequest).getServletRequest());
            Cache cache = methodParameter.getMethodAnnotation(Cache.class);
            redisTemplate.opsForValue().set(redisKey, redisValue,cache.time(), TimeUnit.SECONDS);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        return body;
    }
}
