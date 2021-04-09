package com.itheima.tanhua.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.server.utils.Cache;
import com.itheima.tanhua.server.utils.CacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RedisCacheInterceptor implements HandlerInterceptor {
    @Value("${tanhua.cache.enable}")
    private Boolean isCached;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*判断是否开启缓存*/
        if(!isCached){
            return  true;
        }

        /*判断是否是controller的方法*/
        if (!(handler instanceof HandlerMethod)){
            return true;
        }


        HandlerMethod handlerMethod = (HandlerMethod) handler;
        boolean mappingAnnotation = handlerMethod.hasMethodAnnotation(GetMapping.class);
        /*判断是否是get请求*/
        if(!mappingAnnotation){
            return true;
        }

        boolean cacheAnnotation = handlerMethod.hasMethodAnnotation(Cache.class);

        /*判断是否有Cache注解*/
        if(!cacheAnnotation){
            return true;
        }

        String redisKey = CacheUtils.createRedisKey((HttpServletRequest) request);

        if(redisKey == null){
            return true;
        }

        String value = redisTemplate.opsForValue().get(redisKey);

        if(StringUtils.isEmpty(value)){
            return true;
        }

        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(value);

        return false;
    }
}
