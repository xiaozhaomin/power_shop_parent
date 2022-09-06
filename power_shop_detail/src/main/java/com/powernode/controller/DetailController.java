package com.powernode.controller;

import com.powershop.feign.ItemServiceFeign;
import com.powershop.pojo.TbItem;
import com.powershop.pojo.TbItemDesc;
import com.powershop.pojo.TbItemParamItem;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/frontend/detail")
public class DetailController {
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @RequestMapping("/selectItemInfo")
    public  Result selectItemInfo(Long itemId){
        try {
            TbItem tbItem = itemServiceFeign.selectItemInfo(itemId);
            return Result.ok(tbItem);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }
    @RequestMapping("/selectItemDescByItemId")
    public Result selectItemDescByItemId(Long itemId){
        try {
            TbItemDesc tbItemDesc = itemServiceFeign.selectItemDescByItemId(itemId);
            return Result.ok(tbItemDesc);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    @RequestMapping("/selectTbItemParamItemByItemId")
    public Result selectTbItemParamItemByItemId(Long itemId){
        try {
            TbItemParamItem tbItemParamItem = itemServiceFeign.selectTbItemParamItemByItemId(itemId);
            return Result.ok(tbItemParamItem);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }
}
