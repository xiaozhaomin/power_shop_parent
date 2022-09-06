package com.powershop.controller;

import com.powershop.pojo.TbItemParam;
import com.powershop.pojo.TbItemParamItem;
import com.powershop.service.ItemParamService;
import com.powershop.utils.PageResult;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/itemParam")
public class ItemParamController {

    @Autowired
    private ItemParamService itemParamService;

    /**
     * 根据itemCatId查询商品的规格模板
     * @param itemCatId
     * @return
     */
    @RequestMapping("selectItemParamByItemCatId/{itemCatId}")
    public Result selectItemParamByItemCatId(@PathVariable Long itemCatId){
        try {
            TbItemParam tbItemParam = itemParamService.selectItemParamByItemCatId(itemCatId);
            return Result.ok(tbItemParam);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    /**
     * 分页查询规格模板列表
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectItemParamAll")
    public Result selectItemParamAll(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer rows){
        try {
            PageResult pageResult = itemParamService.selectItemParamAll(page,rows);
            return Result.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    @RequestMapping("insertItemParam")
    public Result insertItemParam(Long itemCatId,String paramData){
        try {
            boolean result = itemParamService.insertItemParam(itemCatId, paramData);
            if(result){
                return Result.ok();
            }
            return Result.error("该类目已有规格模板");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("插入失败");
        }
    }

    @RequestMapping("deleteItemParamById")
    public Result deleteItemParamById(Long id){
        try {
            itemParamService.deleteItemParamById(id);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }
    @RequestMapping("/selectTbItemParamItemByItemId")
    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId){
        return itemParamService.selectTbItemParamItemByItemId(itemId);
    }

}
