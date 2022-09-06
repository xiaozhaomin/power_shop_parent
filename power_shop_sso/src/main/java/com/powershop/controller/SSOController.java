package com.powershop.controller;

import com.powershop.pojo.TbUser;
import com.powershop.service.SsoService;
import com.powershop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/frontend/sso")
public class SSOController {

    @Autowired
    private SsoService ssoService;

    /**
     * 校验用户信息
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @RequestMapping("/checkUserInfo/{checkValue}/{checkFlag}")
    public Result checkUserInfo(@PathVariable String checkValue,
                                @PathVariable Integer checkFlag ){
        try {
            if (ssoService.checkUserInfo(checkValue,checkFlag)){
                return Result.ok();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("校验失败");
    }

    /**
     * 用户注册
     * @param tbUser
     * @return
     */
    @RequestMapping("/userRegister")
    public Result userRegister(TbUser tbUser){
        try {
            ssoService.userRegister(tbUser);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("注册失败");
        }
    }
    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/userLogin")
    public Result userLogin(String username, String password ){
        try {
            Map<String,Object> map = ssoService.userLogin(username,password);
            return Result.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("登陆成功");
    }


    /**
     * 根据token查询用户信息
     * @param token
     * @return
     */
    @RequestMapping("/getUserByToken/{token}")
    public Result getUserByToken(@PathVariable String token ){
        try {
           if (ssoService.getUserByToken(token)){
               return Result.ok();
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("用户未登录");
    }
    @RequestMapping("/logOut")
    public Result logOut(String token ){
        try {
            ssoService.logOut(token);
                return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("退出失败");
    }

    /**
     * 通过token查询用户信息
     * @param token
     * @return
     */
    @RequestMapping("/getUserFromRedis/{token}")
    public boolean getUserFromRedis(@PathVariable("token") String token){
        return ssoService.getUserByToken(token);
    }

}
