package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.handler.CartCookieHandler;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import jdk.nashorn.internal.runtime.RewriteException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {
    @Reference
    CartService cartService;
    @Reference
    ManageService manageService;

    @Autowired
    CartCookieHandler cartCookieHandler;


    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public  String addToCart(HttpServletRequest request, HttpServletResponse response){

        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        //从域中获取userId用于判断用户是否登录
        String userId = (String) request.getAttribute("userId");


        if (!StringUtils.isEmpty(userId)){
            //用户已经登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        } else {
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }

        //获取sku信息对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);


        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletResponse response,HttpServletRequest request){

        String  userId = (String) request.getAttribute("userId");

        if (userId != null){
            //从cookie中查找购物车
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            List<CartInfo> cartList =null;
            if (cartListFromCookie !=null && cartListFromCookie.size()>0){
                //开始合并
              cartList =   cartService.mergeToCartList(cartListFromCookie,userId);
              cartCookieHandler.deleteCartCookie(request,response);
            }else {
                cartList = cartService.getCartList(userId);
            }
          request.setAttribute("cartList",cartList);
        } else {

            List<CartInfo> cartList =    cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);

        }


        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)

    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");
        if (userId !=null){
            cartService.checkCart(skuId,isChecked,userId);
        } else {
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }

    @LoginRequire
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = cartCookieHandler.getCartList(request);
        if(cartList != null && cartList.size()>0){
           cartService.mergeToCartList(cartList,userId);
           cartCookieHandler.deleteCartCookie(request,response);
        }


        return "redirect://order.gmall.com/trade";
    }



}
