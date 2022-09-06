package com.powershop.feign;

import com.powershop.pojo.TbItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
@FeignClient("power-shop-cart")
public interface CartServiceFeign {
    @RequestMapping("/frontend/cart/getCartFromRedis")
    Map<String, TbItem> getCartFromRedis(@RequestParam("userId") Long userId);
}
