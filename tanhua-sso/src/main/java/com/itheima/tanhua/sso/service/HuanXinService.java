package com.itheima.tanhua.sso.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.itheima.tanhua.sso.config.HuanXinConfig;
import com.itheima.tanhua.sso.vo.HuanXinUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class HuanXinService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HuanXinConfig huanXinConfig;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static final String HUAN_XIN_TOKEN = "HUAN_XIN_TOKEN";

    private static final ObjectMapper mapper = new ObjectMapper();

    public String getToken(){
        /**
         * 1. 从redis当中 获取token
         * 2. 如果没有，请求环信 token获取接口 获取token
         * 3. token放入redis
         */
        String redisToken = this.redisTemplate.opsForValue().get(HUAN_XIN_TOKEN);
        if (StringUtils.isNotEmpty(redisToken)){
            return redisToken;
        }
        ///{org_name}/{app_name}/token
        String targetUrl = this.huanXinConfig.getUrl() + this.huanXinConfig.getOrgName() + "/" + this.huanXinConfig.getAppName() + "/token";

        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "client_credentials");
        param.put("client_id", this.huanXinConfig.getClientId());
        param.put("client_secret", this.huanXinConfig.getClientSecret());
        //rest 接收json参数 返回json参数  url  /aa/aa/aa  post get put delete
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, param, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()){
            //
            String body = responseEntity.getBody();
            //access_token	有效的token字符串
            //expires_in	token 有效时间，以秒为单位，在有效期内不需要重复获取
            //application	当前 App 的 UUID 值
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String accessToken = jsonNode.get("access_token").asText();
                Long expireTime = jsonNode.get("expires_in").asLong() - 2*60*60;
                this.redisTemplate.opsForValue().set(HUAN_XIN_TOKEN,accessToken,expireTime, TimeUnit.SECONDS);
                return  accessToken;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 注册环信用户
     *
     * @param userId
     * @return
     */
    public boolean register(Long userId) {
        String targetUrl = this.huanXinConfig.getUrl()
                + this.huanXinConfig.getOrgName() + "/"
                + this.huanXinConfig.getAppName() + "/users";

        String token = getToken();

        try {
            // 请求体
            HuanXinUser huanXinUser = new HuanXinUser(String.valueOf(userId), DigestUtils.md5Hex(userId + "_itcast_tanhua"));
            String body = mapper.writeValueAsString(huanXinUser);

            // 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer " + token);

            HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);

            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 注册失败
        return false;

    }

    /**
     * 添加好友
     * @param userId
     * @param friendId
     * @return
     */
    public boolean contactUsers(Long userId, Long friendId) {
        String targetUrl = this.huanXinConfig.getUrl()
                + this.huanXinConfig.getOrgName() + "/"
                + this.huanXinConfig.getAppName() + "/users/" +
                userId + "/contacts/users/" + friendId;

        String token = getToken();

        try {

            // 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer " + token);

            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);

            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 添加失败
        return false;
    }


    /**
     *发送文本消息
     * @param userId
     * @return
     */
    public boolean sendMsg(Long userId,String type,String msg) {
        String targetUrl = this.huanXinConfig.getUrl()
                + this.huanXinConfig.getOrgName() + "/"
                + this.huanXinConfig.getAppName() + "/messages";

        String token = getToken();

        try {

            // 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer " + token);

            //参数
            Map<String, Object> param = new HashMap<>();
            param.put("target_type", "users");
            param.put("target", Arrays.asList(String.valueOf(userId)));
            //"msg": {"type": "txt","msg": "testmessage"}
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("type",type);
            msgMap.put("msg",msg);
            param.put("msg", msgMap);

            String valueAsString = mapper.writeValueAsString(param);

            HttpEntity<String> httpEntity = new HttpEntity<>(valueAsString,headers);
            ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);

            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 添加失败
        return false;
    }

}