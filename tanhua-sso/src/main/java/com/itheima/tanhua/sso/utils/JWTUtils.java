package com.itheima.tanhua.sso.utils;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {

    public static String token(Map<String, Object> claims,String secret){
        //jwt的头部信息 固定 可解密
        Map<String, Object> header = new HashMap<String, Object>();
        header.put(JwsHeader.TYPE, JwsHeader.JWT_TYPE);
        header.put(JwsHeader.ALGORITHM, "HS256");
        //payload 数据 不建议存放敏感数据

        // 生成token 可以设置过期时间 一旦过期 token可以任务验证不通过
        String jwt = Jwts.builder()
                .setHeader(header)  //header，可省略
                .setClaims(claims) //payload，存放数据的位置，不能放置敏感数据，如：密码等
                .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
//                .setExpiration(new Date(System.currentTimeMillis() + 30000)) //设置过期时间，3秒后过期
                .compact();

        return jwt;
    }
}
