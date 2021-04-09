package com.itheima.tanhua.server.handler;

import com.itheima.tanhua.server.service.SSOService;
import com.itheima.tanhua.server.utils.NoLogin;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.sso.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private SSOService ssoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 拿到token，调用sso token认证
         *     如果是访问 图片，目录等待请求，不用走token认证 直接放行
         *     如果此接口不需要登录呢？直接放行
         */
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.hasMethodAnnotation(NoLogin.class)){
            return true;
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            //未登录 http状态 401
            response.setStatus(401);
            return false;
        }
        User user = ssoService.checkToken(token);
        if (user == null){
            //未登录 http状态 401
            response.setStatus(401);
            return false;
        }
        UserThreadLocal.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}
