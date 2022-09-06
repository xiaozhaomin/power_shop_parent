package com.powershop.service;

import com.powershop.pojo.TbItem;
import com.powershop.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
@Service
@Transactional
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisClient redisClient;
    @Value("${CART_REDIS}")
    private String CART_REDIS;

    /**
     * 根据用户 ID 查询用户购物车
     * @param userId
     * @return
     */
    @Override
    public Map<String, TbItem> getCartFromRedis(String userId) {
        return (Map<String, TbItem>) redisClient.hget(CART_REDIS,userId);
    }

    /**
     * 缓存购物车
     * @param userId
     * @param cart
     * @return
     */
    @Override
    public Boolean insertCart(String userId, Map<String, TbItem> cart) {
        return redisClient.hset(CART_REDIS,userId,cart);
    }
}
