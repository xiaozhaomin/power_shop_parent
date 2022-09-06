package com.powershop.job;

import com.powershop.service.OrderService;
import com.powershop.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class OrderJob {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisClient redisClient;
    @Value("${SETNX_LOCK_ORDER_KEY}")
    private String SETNX_LOCK_ORDER_KEY;;

    public void closeTimeoutOrder(){
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if(redisClient.setnx(SETNX_LOCK_ORDER_KEY,ip)) {
            try {
                orderService.closeTimeoutOrder();
            }finally {
                redisClient.del(SETNX_LOCK_ORDER_KEY);
            }
        }else{
            ip = (String) redisClient.get("SETNX_LOCK_ORDER_KEY");
            System.out.println(
                    "==========机器："+ip+" 占用分布式锁，任务已然执行过了=============");
        }
    }
}
