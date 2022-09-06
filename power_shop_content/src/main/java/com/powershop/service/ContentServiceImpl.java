package com.powershop.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.powershop.mapper.TbContentCategoryMapper;
import com.powershop.mapper.TbContentMapper;
import com.powershop.pojo.TbContent;
import com.powershop.pojo.TbContentCategory;
import com.powershop.pojo.TbContentCategoryExample;
import com.powershop.pojo.TbContentExample;
import com.powershop.utils.AdNode;
import com.powershop.utils.PageResult;
import com.powershop.utils.RedisClient;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;
    @Autowired
    private TbContentMapper tbContentMapper;
    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;
    @Value("${AD_HEIGHT}")
    private Integer AD_HEIGHT;
    @Value("${AD_WIDTH}")
    private Integer AD_WIDTH;
    @Value("${AD_HEIGHTB}")
    private Integer AD_HEIGHTB;
    @Value("${AD_WIDTHB}")
    private Integer AD_WIDTHB;
    @Autowired
    private RedisClient redisClient;
    @Value("${PORTAL_AD}")
    private String PORTAL_AD;

    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        return tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
    }

    @Override
    public void insertContentCategory(TbContentCategory contentCategory) {
        //添加内容分类节点
        contentCategory.setStatus(1);
        contentCategory.setSortOrder(1);
        contentCategory.setIsParent(false);
        contentCategory.setCreated(new Date());
        contentCategory.setUpdated(new Date());
        tbContentCategoryMapper.insert(contentCategory);

        //如果他爹不是爹，要把他爹变成爹
        TbContentCategory tbContentCategoryParent = tbContentCategoryMapper.selectByPrimaryKey(contentCategory.getParentId());
        if(!tbContentCategoryParent.getIsParent()){
            tbContentCategoryParent.setIsParent(true);
            tbContentCategoryParent.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKey(tbContentCategoryParent);
        }
    }

    @Override
    public Result deleteContentCategoryById(Long categoryId) {
        //1、父节点不允许删除
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        if(tbContentCategory.getIsParent()){
            return Result.error("父节点不允许删除");
        }

        //删除节点
        tbContentCategoryMapper.deleteByPrimaryKey(categoryId);

        //2、如果他爹不是爹，要把他爹改成不是爹
        List<TbContentCategory> tbContentCategoryList = this.selectContentCategoryByParentId(tbContentCategory.getParentId());
        if(tbContentCategoryList==null || tbContentCategoryList.size()==0){
            TbContentCategory tbContentCategoryParent = tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());
            tbContentCategoryParent.setIsParent(false);
            tbContentCategoryParent.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKey(tbContentCategoryParent);
        }
        return Result.ok();
    }

    @Override
    public PageResult selectTbContentAllByCategoryId(Integer page, Integer rows, Long categoryId) {
        PageHelper.startPage(page,rows);
        TbContentExample tbContentExample = new TbContentExample();
        TbContentExample.Criteria criteria = tbContentExample.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(tbContentExample);
        PageInfo<TbContent> pageInfo = new PageInfo<>(tbContentList);
        return new PageResult(pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
    }

    @Override
    public void insertTbContent(TbContent tbContent) {
        tbContent.setUpdated(new Date());
        tbContent.setCreated(new Date());
        tbContentMapper.insert(tbContent);
        redisClient.del(PORTAL_AD);
    }

    @Override
    public void deleteContentByIds(Long[] ids) {
        for (Long id : ids) {
            tbContentMapper.deleteByPrimaryKey(id);
        }
        redisClient.del(PORTAL_AD);
    }

    @Override
    public List<AdNode> selectFrontendContentByAD() {
        /******************原则：不能改变原有的逻辑*******************/
        //a、先查询redis，如果有直接返回
        List<AdNode> adNodeList = (List<AdNode>) redisClient.get(PORTAL_AD);
        if(adNodeList!=null && adNodeList.size()>0){
            return adNodeList;
        }
        //b、再查mysql，缓存到redis，并返回
        TbContentExample tbContentExample = new TbContentExample();
        tbContentExample.createCriteria().andCategoryIdEqualTo(98L);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(tbContentExample);
        adNodeList = new ArrayList<>();
        for (TbContent tbContent : tbContentList) {
            AdNode adNode = new AdNode();
            adNode.setHref(tbContent.getUrl());
            adNode.setSrc(tbContent.getPic());
            adNode.setHeight(AD_HEIGHT);
            adNode.setWidth(AD_WIDTH);
            adNode.setHeightB(AD_HEIGHTB);
            adNode.setWidthB(AD_WIDTHB);
            adNodeList.add(adNode);
        }
        redisClient.set(PORTAL_AD,adNodeList);
        return adNodeList;
    }
}
