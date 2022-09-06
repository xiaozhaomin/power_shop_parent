package com.powershop.feign;


import com.powershop.pojo.TbItem;
import com.powershop.pojo.TbItemDesc;
import com.powershop.pojo.TbItemParamItem;
import com.powershop.utils.CatResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("power-shop-item")
public interface ItemServiceFeign {

    @RequestMapping("/backend/itemCategory/selectItemCategoryAll")
    CatResult selectItemCategoryAll();

    @RequestMapping("/backend/item/selectItemInfo")
    TbItem selectItemInfo(@RequestParam("itemId") Long itemId);

    @RequestMapping("/backend/item/selectItemDescByItemId")
    TbItemDesc selectItemDescByItemId(@RequestParam("itemId") Long itemId);

    @RequestMapping("/backend/itemParam/selectTbItemParamItemByItemId")
    TbItemParamItem selectTbItemParamItemByItemId(@RequestParam("itemId") Long itemId);

    @RequestMapping("/backend/item/updateItemNumByItemId")
    void updateItemNumByItemId(@RequestParam("itemId") Long itemId, @RequestParam("num") Integer num);
}
