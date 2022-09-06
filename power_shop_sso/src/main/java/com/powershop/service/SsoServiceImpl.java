package com.powershop.service;

import com.powershop.mapper.TbUserMapper;
import com.powershop.pojo.TbUser;
import com.powershop.pojo.TbUserExample;
import com.powershop.utils.MD5Utils;
import com.powershop.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SsoServiceImpl implements SsoService {
    @Autowired
    private TbUserMapper tbUserMapper;
    @Autowired
    private RedisClient redisClient;
    @Value("${USER_INFO}")
    private String USER_INFO;
    @Value("${SESSION_EXPIRE}")
    private Integer SESSION_EXPIRE;

    /**
     * 对用户的注册信息(用户名与电话号码)做数据校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @Override
    public boolean checkUserInfo(String checkValue, Integer checkFlag) {
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        // 1、查询条件根据参数动态生成：1、2分别代表username、phone
        if (checkFlag ==1){
            criteria.andCreatedEqualTo(checkValue);
        }else if (checkFlag == 2){
            criteria.andCreatedEqualTo(checkValue);
        }
        // 2、从tb_user表中查询数据
        List<TbUser> tbUsersList = tbUserMapper.selectByExample(tbUserExample);
        // 3、判断查询结果
        if (tbUsersList!=null && tbUsersList.size()>0){
            return false;
        }
        return true;
    }

    /**
     * 用户注册
     * @param tbUser
     */
    @Override
    public void userRegister(TbUser tbUser) {
        //补齐数据 将密码做加密处理。
        tbUser.setCreated(new Date());
        tbUser.setUpdated(new Date());
        tbUser.setPassword(MD5Utils.digest(tbUser.getPassword()));
        tbUserMapper.insertSelective(tbUser);
    }

    @Override
    public Map<String, Object> userLogin(String username, String password) {
        //1、密码加密
        String pwd = MD5Utils.digest(password);
        //2、校验用户名密码
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        criteria.andUsernameEqualTo(username);
        criteria.andPasswordEqualTo(pwd);
        List<TbUser> tbUserList = tbUserMapper.selectByExample(tbUserExample);
        if(tbUserList==null || tbUserList.size()==0){
            return null;
        }
        //3、把user缓存到redis
        TbUser tbUser = tbUserList.get(0);
        tbUser.setPassword(null);
        String uuid = UUID.randomUUID().toString();
        redisClient.set(USER_INFO+":"+uuid,tbUser);
        //4、设置过期时间
        redisClient.expire(USER_INFO+":"+uuid,SESSION_EXPIRE);
        //5、返回map
        HashMap<String, Object> map = new HashMap<>();
        map.put("token",uuid);
        map.put("userid",tbUser.getId());
        map.put("username",tbUser.getUsername());
        return map;
    }

    @Override
    public boolean getUserByToken(String token) {
        TbUser tbUser = (TbUser) redisClient.get(USER_INFO + ":" + token);
        if(tbUser==null) {
            return false;
        }
        redisClient.expire(USER_INFO+":"+token,SESSION_EXPIRE);
        return true;
    }

    @Override
    public void logOut(String token) {
        redisClient.del(USER_INFO+":"+token);
    }
}
