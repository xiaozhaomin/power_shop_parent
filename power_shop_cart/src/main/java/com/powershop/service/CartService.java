package com.powershop.service;

import com.powershop.pojo.TbItem;

import java.util.Map;

public interface CartService {
    Map<String, TbItem> getCartFromRedis(String userId);

    Boolean insertCart(String userId, Map<String, TbItem> cart);

}
