package com.itheima.tanhua.sso.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.sso.config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SendSMS {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private RestTemplate restTemplate;

    public boolean send(String mobile,int verifyCode){
        String url = "https://open.ucpaas.com/ol/sms/sendsms";
        //{
        // "sid":"39467b989d087c2d92c6132184a365d8",
        // "token":"23f757bad208226ec301e117e40006ed",
        // "appid":"2d92c6132139467b989d087c84a365d8",
        // "templateid":"154501",
        // "param":"87828,3",
        // "mobile":"18011984299",
        // "uid":"2d92c6132139467b989d087c84a365d8"
        //}
        Map<String,String> params = new HashMap<>();
        params.put("sid", "0c71daeb7f24a6ef768cb00b132c5624");
        params.put("token", "38c5b2f652edec2cd5d7d26e58ab510e");
        params.put("appid", "70cabdc8fa3349429417d0580bcead2f");
        params.put("templateid", "564697");
        params.put("mobile", mobile);

        System.out.println(verifyCode);
        params.put("param", String.valueOf(verifyCode)+",300");

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, params, String.class);
        //http状态码 2开头 是代表成功 404 接口找不到，401 未认证，500 服务器错误
        //400  参数传递格式有误 403 登录成功了，但是无权限
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            //发短信成功了 body是json字符串
            String body = responseEntity.getBody();
            //{
            //   "code":"0",
            //   "msg":"OK",
            //   "count":"1",
            //   "create_date":"2017-08-28 19:08:28",
            //   "smsid":"f96f79240e372587e9284cd580d8f953",
            //   "mobile":"18011984299",
            //   "uid":"2d92c6132139467b989d087c84a365d8"
            //}
            JsonNode jsonNode = null;
            try {
                jsonNode = MAPPER.readTree(body);
                JsonNode code = jsonNode.get("code");
                if (code.asText().equals("000000")){
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
       /* String url = "https://open.ucpaas.com/ol/sms/sendsms";

        //{
        // "sid":"39467b989d087c2d92c6132184a365d8",
        // "token":"23f757bad208226ec301e117e40006ed",
        // "appid":"2d92c6132139467b989d087c84a365d8",
        // "templateid":"154501",
        // "param":"87828,3",
        // "mobile":"18011984299",
        // "uid":"2d92c6132139467b989d087c84a365d8"
        //}

        Map<String,String> map = new HashMap<>();
        map.put("sid","da1758dc3a3d798c6824930bf3d6c8c3");
        map.put("token","1854cbb8ed0d05efda420d7d74bcc153");
        map.put("appid", "61a73d73ee29459aa6c80744642f8daa");
        map.put("templateid", "564697");
        map.put("mobile", mobile);

        System.out.println(verifyCode);
        map.put("param", String.valueOf(verifyCode)+",300");

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, map, String.class);

        if(responseEntity.getStatusCode().is2xxSuccessful()){
            String body = responseEntity.getBody();
            JsonNode jsonNode = null;
            try {
                jsonNode  = MAPPER.readTree(body);
                JsonNode code = jsonNode.get("code");
                if(code.asText().equals("000000")){
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;*/
    }

}
