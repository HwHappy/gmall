package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Controller
public class OrderController {
    @Reference
    UserService userService;

    @Reference
    CartService cartService;
    @Reference
    OrderService orderService;

    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //获取用户对应的地址信息
        List<UserAddress> addressList = userService.getAddressList(userId);

        request.setAttribute("addressList",addressList);
        //获取订单信息
        List<CartInfo> cartList = cartService.getCheckedCartList(userId);
        //创建订单详情的集合
        List<OrderDetail> orderDetailList = new ArrayList<>(cartList.size());
        for (CartInfo cartInfo : cartList) {
            //创建订单详情对象
            OrderDetail orderDetail = new OrderDetail();
            //给对象赋值
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //把对象放到集合中
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);

        //计算所有选中商品的总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        BigDecimal totalAmount =orderInfo.getTotalAmount();
        request.setAttribute("totalAmount",totalAmount);

        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeCode",tradeNo);

        return "trade";
    }


    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(HttpServletRequest request,OrderInfo orderInfo){


        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.verifyTradeNo(userId, tradeNo);
        if (!flag){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }
        orderService.delTradeNo(userId);

        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result) {
                request.setAttribute("errMsg", "商品库存不足，请重新下单！");
                return "tradeFail";
            }
        }
        String orderId = orderService.saveOrder(orderInfo);


        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }



}
