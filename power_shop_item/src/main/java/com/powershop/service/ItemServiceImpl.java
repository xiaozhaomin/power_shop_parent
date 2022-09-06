package com.powershop.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.powershop.mapper.LocalMessageMapper;
import com.powershop.mapper.TbItemDescMapper;
import com.powershop.mapper.TbItemMapper;
import com.powershop.mapper.TbItemParamItemMapper;
import com.powershop.pojo.*;
import com.powershop.utils.IDUtils;
import com.powershop.utils.PageResult;
import com.powershop.utils.RedisClient;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private TbItemDescMapper tbItemDescMapper;
    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;
//    @Autowired
//    private AmqpTemplate amqpTemplate;
    @Autowired
    private LocalMessageMapper localMessageMapper;
    @Autowired
    private RedisClient redisClient;
    @Value("${ITEM_INFO}")
    private String ITEM_INFO;
    @Value("${BASE}")
    private String BASE;
    @Value("${DESC}")
    private String DESC;
    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;
    @Value("${SETNX_LOCK_BASC}")
    private String SETNX_LOCK_BASC;
    @Value("${SETNX_LOCK_DESC}")
    private String SETNX_LOCK_DESC;



    @Override
    public TbItem selectItemInfo(Long itemId) {
        /*****************不能修改原理的逻辑*****************/
        //1、先查redis，如果有直接return
        TbItem tbItem = (TbItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + BASE);
        if(tbItem != null){
            return tbItem;
        }
        /*****************解决缓存击穿**********************/
        if(redisClient.setnx(SETNX_LOCK_BASC+":"+itemId,itemId)) {
            try {
                //2、再查mysql，并缓存到redis再rerun
                tbItem = tbItemMapper.selectByPrimaryKey(itemId);
                //解决缓存穿透问题
                if (tbItem == null) {
                    tbItem = new TbItem();
                    redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE, tbItem);
                    redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE, 30);
                    return tbItem;
                }
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE, tbItem);
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE, ITEM_INFO_EXPIRE);
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                //釋放鎖
                redisClient.del(SETNX_LOCK_BASC + ":" + itemId);
            }
        }else{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            selectItemInfo(itemId);
        }
        return tbItem;
    }

    @Override
    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);

        TbItemExample tbItemExample = new TbItemExample();
        tbItemExample.setOrderByClause("updated DESC");
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusNotEqualTo((byte)3);
        List<TbItem> tbItemList = tbItemMapper.selectByExample(tbItemExample);

        PageInfo<TbItem> pageInfo = new PageInfo<>(tbItemList);

        return new PageResult(pageInfo.getPageNum(),pageInfo.getPages(),pageInfo.getList());
    }

    @Override
    public void insertTbItem(TbItem tbItem, String desc, String itemParams) {
        //1、插入商品信息
        long itemId = IDUtils.genItemId();
        tbItem.setId(itemId);
        tbItem.setPrice(tbItem.getPrice()*100);
        tbItem.setStatus((byte)1);
        tbItem.setCreated(new Date());
        tbItem.setUpdated(new Date());
        tbItemMapper.insert(tbItem);

        //2、插入商品描述信息
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setCreated(new Date());
        tbItemDesc.setUpdated(new Date());
        tbItemDescMapper.insert(tbItemDesc);

        //3、插入规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setCreated(new Date());
        tbItemParamItem.setUpdated(new Date());
        tbItemParamItemMapper.insert(tbItemParamItem);

        //添加商品发布消息到mq
        //amqpTemplate.convertAndSend("item_exchage","item.add",itemId);

        //把消息记录到本地表中
        LocalMessage localMessage = new LocalMessage();
        //主键
        localMessage.setTxNo(UUID.randomUUID().toString());
        //id
        localMessage.setItemId(itemId);
        //状态
        localMessage.setState(0);
        localMessageMapper.insert(localMessage);

    }

    @Override
    public void deleteItemById(Long itemId) {
        TbItem tbItem = new TbItem();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)3);
        tbItemMapper.updateByPrimaryKeySelective(tbItem);
    }

    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId) {
        /*****************不能修改原理的逻辑*****************/
        //1、先查redis，如果有直接return
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(ITEM_INFO + ":" + itemId + ":" + DESC);
        if(tbItemDesc != null){
            return tbItemDesc;
        }
        //2、再查mysql，并缓存到redis再rerun
        tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        //解决缓存穿透问题
        if(tbItemDesc == null){
            tbItemDesc = new TbItemDesc();
            redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,tbItemDesc);
            redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,30);
            return tbItemDesc;
        }
        redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,tbItemDesc);
        redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,ITEM_INFO_EXPIRE);
        return tbItemDesc;
    }

    @Override
    public void updateItemNumByItemId(Long itemId, Integer num) {
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        if(tbItem.getNum()<num){
            throw new RuntimeException("库存不足");
        }
        tbItem.setNum(tbItem.getNum()-num);
        tbItemMapper.updateByPrimaryKey(tbItem);
    }
}
