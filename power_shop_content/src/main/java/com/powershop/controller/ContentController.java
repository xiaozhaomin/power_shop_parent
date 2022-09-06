package com.powershop.controller;

import com.powershop.pojo.TbContent;
import com.powershop.pojo.TbContentCategory;
import com.powershop.service.ContentService;
import com.powershop.utils.AdNode;
import com.powershop.utils.PageResult;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    /**
     * 查询内容类别节点
     * @param id
     * @return
     */
    @RequestMapping("/selectContentCategoryByParentId")
    public Result selectContentCategoryByParentId(@RequestParam(defaultValue = "0") Long id){
        try {
            List<TbContentCategory> tbContentCategoryList = contentService.selectContentCategoryByParentId(id);
            return Result.ok(tbContentCategoryList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    /**
     * 添加内容分类节点
     * @param contentCategory
     * @return
     */
    @RequestMapping("/insertContentCategory")
    public Result insertContentCategory(TbContentCategory contentCategory){
        try {
            contentService.insertContentCategory(contentCategory);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }

    /**
     * 删除内容分类节点
     * @param categoryId
     * @return
     */
    @RequestMapping("/deleteContentCategoryById")
    public Result deleteContentCategoryById(Long categoryId){
        try {
            return contentService.deleteContentCategoryById(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 分页查询内容列表
     * @param page
     * @param rows
     * @param categoryId
     * @return
     */
    @RequestMapping("/selectTbContentAllByCategoryId")
    public Result selectTbContentAllByCategoryId(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer rows,
                                                 Long categoryId){
        try {
             PageResult pageResult = contentService.selectTbContentAllByCategoryId(page,rows,categoryId);
             return Result.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    /**
     * 添加内容
     * @param tbContent
     * @return
     */
    @RequestMapping("/insertTbContent")
    public Result insertTbContent(TbContent tbContent){
        try {
            contentService.insertTbContent(tbContent);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }

    /**
     * 按ids删除内容
     * @param ids
     * @return
     */
    @RequestMapping("/deleteContentByIds")
    public Result deleteContentByIds(Long[] ids){
        try {
            contentService.deleteContentByIds(ids);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }
    @RequestMapping("/selectFrontendContentByAD")
    public List<AdNode> selectFrontendContentByAD(){
        return contentService.selectFrontendContentByAD();
    }
}
