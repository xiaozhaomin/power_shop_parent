package com.powershop.service;

import com.powershop.pojo.TbOrder;
import com.powershop.pojo.TbOrderShipping;

public interface OrderService {

    Long insertOrder(String orderItem, TbOrder tbOrder, TbOrderShipping tbOrderShipping);

    void closeTimeoutOrder();

}
