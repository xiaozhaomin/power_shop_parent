package com.powershop.service;

import com.powershop.mapper.TbItemCatMapper;
import com.powershop.pojo.TbItemCat;
import com.powershop.pojo.TbItemCatExample;
import com.powershop.utils.CatNode;
import com.powershop.utils.CatResult;
import com.powershop.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper;
    @Autowired
    private RedisClient redisClient;
    @Value("${PROTAL_CATRESULT}")
    private String PROTAL_CATRESULT;

    @Override
    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        return tbItemCatMapper.selectByExample(tbItemCatExample);
    }

    @Override
    public CatResult selectItemCategoryAll() {
        /*****************原则：不能改变原有的逻辑****************/
        //a、先查询redis，如果有直接返回
        CatResult catResult = (CatResult) redisClient.get(PROTAL_CATRESULT);
        if(catResult != null){
            return catResult;
        }
        //b、再查mysql，缓存到redis，并返回
        List<Object> catNodeList = getCatNodeList(0L);
        catResult = new CatResult();
        catResult.setData(catNodeList);
        redisClient.set(PROTAL_CATRESULT,catResult);
        return catResult;
    }

    private List<Object> getCatNodeList(Long itemCatId) {
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(itemCatId);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(tbItemCatExample);
        List<Object> catNodeList = new ArrayList<>();
        for (TbItemCat tbItemCat : tbItemCatList) {
            if(tbItemCat.getIsParent()) {
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getCatNodeList(tbItemCat.getId()));
                catNodeList.add(catNode);
            }else {
                catNodeList.add(tbItemCat.getName());
            }
        }
        return catNodeList;
    }
}
