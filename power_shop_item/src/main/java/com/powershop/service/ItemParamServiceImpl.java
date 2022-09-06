package com.powershop.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.powershop.mapper.TbItemParamItemMapper;
import com.powershop.mapper.TbItemParamMapper;
import com.powershop.pojo.TbItemParam;
import com.powershop.pojo.TbItemParamExample;
import com.powershop.pojo.TbItemParamItem;
import com.powershop.pojo.TbItemParamItemExample;
import com.powershop.utils.PageResult;
import com.powershop.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {

    @Autowired
    private TbItemParamMapper tbItemParamMapper;
    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;
    @Value("${ITEM_INFO}")
    private String ITEM_INFO;
    @Value("${PARAM}")
    private String PARAM;
    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;
    @Autowired
    private RedisClient redisClient;


    @Override
    public TbItemParam selectItemParamByItemCatId(Long itemCatId) {
        TbItemParamExample tbItemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = tbItemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        return tbItemParamMapper.selectByExampleWithBLOBs(tbItemParamExample).get(0);
    }

    @Override
    public PageResult selectItemParamAll(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        List<TbItemParam> tbItemParamList = tbItemParamMapper.selectByExampleWithBLOBs(null);
        PageInfo<TbItemParam> pageInfo = new PageInfo<>(tbItemParamList);
        return new PageResult(pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
    }

    @Override
    public boolean insertItemParam(Long itemCatId, String paramData) {
        //判断该类型是否已然添加过模板
        TbItemParamExample tbItemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = tbItemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> tbItemParamList = tbItemParamMapper.selectByExample(tbItemParamExample);
        if(tbItemParamList!=null && tbItemParamList.size()>0){
            return false;
        }
        TbItemParam tbItemParam = new TbItemParam();
        tbItemParam.setCreated(new Date());
        tbItemParam.setUpdated(new Date());
        tbItemParam.setItemCatId(itemCatId);
        tbItemParam.setParamData(paramData);
        tbItemParamMapper.insert(tbItemParam);
        return true;
    }

    @Override
    public void deleteItemParamById(Long id) {
        tbItemParamMapper.deleteByPrimaryKey(id);
    }

    @Override
    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId) {
        /*****************不能修改原理的逻辑*****************/
        //1、先查redis，如果有直接return
        TbItemParamItem tbItemParamItem = (TbItemParamItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + PARAM);
        if(tbItemParamItem != null){
            return tbItemParamItem;
        }
        //2、再查mysql，并缓存到redis再rerun
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        tbItemParamItem = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample).get(0);
        //解决缓存穿透问题
        if(tbItemParamItem == null){
            tbItemParamItem = new TbItemParamItem();
            redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM,tbItemParamItem);
            redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM,30);
            return tbItemParamItem;
        }
        redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM,tbItemParamItem);
        redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM,ITEM_INFO_EXPIRE);
        return tbItemParamItem;
    }
}
