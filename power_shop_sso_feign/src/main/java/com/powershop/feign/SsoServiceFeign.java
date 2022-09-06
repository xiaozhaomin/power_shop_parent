package com.powershop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("power-shop-sso")
public interface SsoServiceFeign {
    @RequestMapping("frontend/sso/getUserFromRedis/{token}")
    boolean getUserFromRedis(@PathVariable("token") String token);
}
