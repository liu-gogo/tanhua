package com.itheima.tanhua.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public class CacheUtils {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static String createRedisKey(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        String parameterStr = null;
        try {
            parameterStr = MAPPER.writeValueAsString(parameterMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        String token = request.getHeader("Authorization");
        String key = request.getRequestURI() + parameterStr + token;
        String md5Hex = DigestUtils.md5Hex(key);
        return "SERVER_DATA_CACHE_" + key;


    }
}
