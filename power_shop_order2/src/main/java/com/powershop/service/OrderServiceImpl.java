package com.powershop.service;

import com.powershop.feign.ItemServiceFeign;
import com.powershop.mapper.TbOrderItemMapper;
import com.powershop.mapper.TbOrderMapper;
import com.powershop.mapper.TbOrderShippingMapper;
import com.powershop.pojo.TbOrder;
import com.powershop.pojo.TbOrderItem;
import com.powershop.pojo.TbOrderItemExample;
import com.powershop.pojo.TbOrderShipping;
import com.powershop.utils.JsonUtils;
import com.powershop.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Value("${ORDER_ID}")
    private String ORDER_ID;
    @Value("${ORDER_ID_BEGIN}")
    private Long ORDER_ID_BEGIN;
    @Value("${ORDER_ITEM_ID}")
    private String ORDER_ITEM_ID;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private TbOrderMapper tbOrderMapper;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;
    @Autowired
    private TbOrderShippingMapper tbOrderShippingMapper;
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @Override
    public Long insertOrder(String orderItem, TbOrder tbOrder, TbOrderShipping tbOrderShipping) {
        //1、解析
        List<TbOrderItem> tbOrderItemList = JsonUtils.jsonToList(orderItem, TbOrderItem.class);
        //2、保存订单信息
        if (!redisClient.exists(ORDER_ID)){
            redisClient.set(ORDER_ID,ORDER_ID_BEGIN);
        }
        long orderId = redisClient.incr(ORDER_ID, 1L);
        tbOrder.setOrderId(String.valueOf(orderId));
        Date date = new Date();

        tbOrder.setCreateTime(date);
        tbOrder.setUpdateTime(date);
        //1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
        tbOrder.setStatus(1);
        tbOrderMapper.insertSelective(tbOrder);
        //3、保存明细信息
        for (int i = 0; i < tbOrderItemList.size(); i++) {
            Long oderItemId = redisClient.incr(ORDER_ITEM_ID, 1L);
            TbOrderItem tbOrderItem =  tbOrderItemList.get(i);
            tbOrderItem.setId(oderItemId.toString());
            tbOrderItem.setOrderId(String.valueOf(orderId));
            tbOrderItemMapper.insertSelective(tbOrderItem);
            itemServiceFeign.updateItemNumByItemId(Long.parseLong(tbOrderItem.getItemId()),tbOrderItem.getNum());
        }
        //4、保存物流信息
        tbOrderShipping.setOrderId(String.valueOf(orderId));
        tbOrderShipping.setCreated(date);
        tbOrderShipping.setUpdated(date);
        tbOrderShippingMapper.insertSelective(tbOrderShipping);

        //5、返回订单id
        return orderId;
    }

    @Override
    public void closeTimeoutOrder() {
        //1、查询超时订单：线上付款  状态未付款  创建时间<=now()-2
        List<TbOrder> tbOrderList = tbOrderMapper.selectTimeoutOrder();
        //2、关闭超时订单：状态已关闭6  修改时间  关闭时间
        for (TbOrder tbOrder : tbOrderList) {
            tbOrder.setStatus(6);
            tbOrder.setUpdateTime(new Date());
            tbOrder.setCloseTime(new Date());
            tbOrderMapper.updateByPrimaryKey(tbOrder);
            //3、把库存加回去
            TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
            TbOrderItemExample.Criteria criteria = tbOrderItemExample.createCriteria();
            criteria.andOrderIdEqualTo(tbOrder.getOrderId());
            List<TbOrderItem> tbOrderItemList = tbOrderItemMapper.selectByExample(tbOrderItemExample);
            for (TbOrderItem tbOrderItem : tbOrderItemList) {
                itemServiceFeign.updateItemNumByItemId(Long.parseLong(tbOrderItem.getItemId()),(0-tbOrderItem.getNum()));
            }
        }
    }
}
