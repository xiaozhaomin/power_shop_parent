package com.powershop.service;

import com.powershop.pojo.TbContent;
import com.powershop.pojo.TbContentCategory;
import com.powershop.utils.AdNode;
import com.powershop.utils.PageResult;
import com.powershop.utils.Result;

import java.util.List;

public interface ContentService {
    List<TbContentCategory> selectContentCategoryByParentId(Long id);

    void insertContentCategory(TbContentCategory contentCategory);

    Result deleteContentCategoryById(Long categoryId);

    PageResult selectTbContentAllByCategoryId(Integer page, Integer rows, Long categoryId);

    void insertTbContent(TbContent tbContent);

    void deleteContentByIds(Long[] ids);

    List<AdNode> selectFrontendContentByAD();
}
