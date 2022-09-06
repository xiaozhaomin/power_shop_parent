package com.powershop.interceptor;

import com.powershop.feign.SsoServiceFeign;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class UserLoginInterceptor implements HandlerInterceptor {
    @Autowired
    private SsoServiceFeign ssoServiceFeign;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("token");
        if (StringUtils.isBlank(token)){
            return false;
        }
        return ssoServiceFeign.getUserFromRedis(token);
    }
}
