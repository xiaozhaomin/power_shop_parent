package com.powershop.service;

import com.powershop.pojo.TbUser;

import java.util.Map;

public interface SsoService {
    boolean checkUserInfo(String checkValue, Integer checkFlag);

    void userRegister(TbUser tbUser);

    Map<String, Object> userLogin(String username, String password);

    boolean getUserByToken(String token);

    void logOut(String token);
}
