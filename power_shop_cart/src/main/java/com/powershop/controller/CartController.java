package com.powershop.controller;

import com.powershop.feign.ItemServiceFeign;
import com.powershop.pojo.TbItem;
import com.powershop.service.CartService;
import com.powershop.utils.CookieUtils;
import com.powershop.utils.JsonUtils;
import com.powershop.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/frontend/cart")
public class CartController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;
    @Autowired
    private CartService cartService;
    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

    @RequestMapping("/addItem")
    public Result addItem(String userId, String itemId,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam(defaultValue = "1") Integer num){
        try {
            if(StringUtils.isBlank(userId)){
                /***********在用户未登录的状态下**********/
                // 1、从cookie中查询商品列表。
                Map <String, TbItem> cart = getCartFromCookie(request);
                //2、添加商品到购物车
                addItemToCart(itemId,cart,num);
                //3、把购车商品列表写入cookie
                addCartToCookie(cart,request,response);
                return Result.ok();
            }else {
                //登陆操作购物车
                /***********在用户已登录的状态**********/
                // 1、从redis中查询商品列表。
                Map<String,TbItem> cart = getCartFromRedis(userId);
                //2、将商品添加大购物车中
                this.addItemToCart(itemId,cart,num);
                //3、将购物车缓存到 redis 中
                Boolean addCartToRedis = this.addCartToRedis(userId, cart);
                if (addCartToRedis){
                    return Result.ok();
                }
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("添加失败");
    }
    private Boolean addCartToRedis(String userId, Map<String, TbItem> cart) {
        return cartService.insertCart(userId,cart);
    }

    private Map<String, TbItem> getCartFromRedis(String userId) {
        Map <String,TbItem>cart = cartService.getCartFromRedis(userId);
        if (cart!=null && cart.size()>0){
            return cart;
        }
        return new HashMap<String,TbItem>();
    }

    private void addCartToCookie(Map<String, TbItem> cart, HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.setCookie(request,response,CART_COOKIE_KEY,JsonUtils.objectToJson(cart),true);
    }

    /**
     * 把商品添加到购物车
     * @param itemId
     * @param cart
     * @param num
     */
    private void addItemToCart(String itemId, Map<String, TbItem> cart, Integer num) {

        TbItem tbItem = cart.get(itemId);
        if(tbItem != null){
            //1、购物车已存在该商品
            tbItem.setNum(tbItem.getNum()+num);
            cart.put(itemId,tbItem);
        }else{
            //2、购物车不存在该商品
            tbItem = itemServiceFeign.selectItemInfo(Long.parseLong(itemId));
            tbItem.setNum(num);
            cart.put(itemId,tbItem);
        }
    }

    /**
     * 从cookie查找购物车
     * @param request
     * @return
     */
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cartJson = CookieUtils.getCookieValue(request,CART_COOKIE_KEY,true);
        Map map = JsonUtils.jsonToMap(cartJson, TbItem.class);
        if (map!=null &&map.size()>0){
            return map;
        }
        //2、第一次请求查找不到
        return new HashMap<String, TbItem>();
    }
    /**
     * 查看购物车
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/showCart")
    public Result showCart(String userId,
                           HttpServletRequest request,
                           HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                /***********在用户未登录的状态下**********/
                //在用户未登录的状态下
                List<TbItem> list = new ArrayList<TbItem>();
                Map<String,TbItem> cart = this.getCartFromCookie(request);
                Set<String> keys = cart.keySet();
                for(String key :keys){
                    list.add(cart.get(key));
                }
                return Result.ok(list);
            }else {
                ArrayList<TbItem> list = new ArrayList<>();
                Map<String, TbItem> cart = this.getCartFromRedis(userId);
                Set<String> keys = cart.keySet();
                for (String key : keys) {
                    list.add(cart.get(key));
                }
                return Result.ok(list);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.error("查看失败");
    }

    /**
     * 修改购物车商品数量
     * @param itemId
     * @param userId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/updateItemNum")
    public Result updateItemNum(Long itemId, String userId, Integer num,
                                HttpServletRequest request,
                                HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                /***********在用户未登录的状态下**********/
                //未登录
                //1、获得cookie中的购物车
                Map<String, TbItem> cart = getCartFromCookie(request);
                //2、修改购物车中的商品
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                cart.put(itemId.toString(),tbItem);
                //3、把购物车写到cookie
                addCartToCookie(cart,request,response);
                return Result.ok();

            }else {
                // 在用户已登录的状态
                //1、获得cookie的购物车
                Map<String, TbItem> cart = getCartFromRedis(userId);
                //2、修改购物车中的商品
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                //3、把购物车写到redis
                addCartToRedis(userId, cart);
                return Result.ok();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("修改失败");
    }

    /**
     * 删除购物车数据
     * @param itemId
     * @param userId
     * @param num
     * @param request
     * @param respons
     * @return
     */
    @RequestMapping("/deleteItemFromCart")
    public Result deleteItemFromCart(Long itemId, String userId, Integer num,
                                     HttpServletRequest request,
                                     HttpServletResponse respons){
        try {
            if (StringUtils.isBlank(userId)){
                Map<String, TbItem> cart = this.getCartFromCookie(request);
                cart.remove(itemId.toString());
                this.addCartToCookie(cart,request,respons);
                return Result.ok();
            }else {
                Map<String, TbItem> cart = getCartFromRedis(userId);
                cart.remove(itemId.toString());
                addCartToRedis(userId, cart);
            }
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("删除失败");
    }

    @RequestMapping("/getCartFromRedis")
    public Map<String, TbItem> getCartFromRedis(Long userId){
        return cartService.getCartFromRedis(String.valueOf(userId));
    }

}
