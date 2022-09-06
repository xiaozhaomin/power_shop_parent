package com.powershop.service;

import com.powershop.pojo.TbItem;
import com.powershop.pojo.TbItemDesc;
import com.powershop.utils.PageResult;

public interface ItemService {
    TbItem selectItemInfo(Long itemId);

    PageResult selectTbItemAllByPage(Integer page, Integer rows);

    void insertTbItem(TbItem tbItem, String desc, String itemParams);

    void deleteItemById(Long itemId);

    TbItemDesc selectItemDescByItemId(Long itemId);

    void updateItemNumByItemId(Long itemId, Integer num);
}
