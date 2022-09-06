package com.powershop.controller;

import com.powershop.pojo.SearchItem;
import com.powershop.service.SearchItemService;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/frontend/searchItem")
public class SearchItemController {
    @Autowired
    private SearchItemService searchItemService;
    @RequestMapping("/importAll")
    public Result importAll(){
        try {
            searchItemService.importAll();
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }
    @RequestMapping("/list")
    public List<SearchItem> list(String q,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "20") Integer size){
        return searchItemService.list(q,page,size);
    }
}
