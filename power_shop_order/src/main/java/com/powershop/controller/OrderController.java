package com.powershop.controller;

import com.powershop.feign.CartServiceFeign;
import com.powershop.pojo.TbItem;
import com.powershop.pojo.TbOrder;
import com.powershop.pojo.TbOrderShipping;
import com.powershop.service.OrderService;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/frontend/order")
public class OrderController {
    @Autowired
    private CartServiceFeign cartServiceFeign;
    @Autowired
    private OrderService orderService;

    /**
     * 展示订单确认页
     * @param ids
     * @param userId
     * @return
     */
    @RequestMapping("/goSettlement")
    public Result goSettlement(String[] ids, String userId){
        try {
            Map<String, TbItem> cart = cartServiceFeign.getCartFromRedis(Long.valueOf(userId));
            List<TbItem> tbItemList = new ArrayList<>();
            for (String itemId : ids) {
                TbItem tbItem = cart.get(itemId);
                tbItemList.add(tbItem);
            }
            return Result.ok(tbItemList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }
    @RequestMapping("insertOrder")
    public Result insertOrder(String orderItem, TbOrder tbOrder , TbOrderShipping tbOrderShipping){
        Long orderId = orderService.insertOrder(orderItem,tbOrder,tbOrderShipping);
        if (orderId != null){
            return Result.ok(orderId);
        }
        return Result.error("fix提交失败");
    }
}
