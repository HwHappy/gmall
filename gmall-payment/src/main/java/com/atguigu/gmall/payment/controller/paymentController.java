package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class paymentController {

    @Reference
    OrderService orderService;

    @Reference
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(HttpServletRequest request){

        String orderId = request.getParameter("orderId");

      OrderInfo orderInfo =  orderService.getOrderInfo(orderId);

      request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        request.setAttribute("orderId",orderId);
        return "index";

    }

    /**
     * 生成支付二维码的方法
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("alipay/submit")
    @ResponseBody
    public  String submit(HttpServletRequest request, HttpServletResponse response){

        //创建支付信息对象
        PaymentInfo paymentInfo = new PaymentInfo();
        //获取订单号
        String orderId = request.getParameter("orderId");
        //从数据库中获取订单N信息
        OrderInfo orderInfo =  orderService.getOrderInfo(orderId);
        //给支付信息对象赋值
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("phone 7 plus");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());
        //保存支付信息
        paymentService.savePaymentInfo(paymentInfo);
        //创建二维码
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //同步回调的路径
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //异步回调的路径
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
//        创建参数集合并设置参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());
        alipayRequest.setBizContent(JSON.toJSONString(map));

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + AlipayConfig.charset);
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    /**
     * 同步回调
     * @return
     */
    @RequestMapping("alipay/callback/return")
    public String callbackReturn(){

        return "redirect:"+AlipayConfig.return_order_url;
    }

    /**
     * 异步回调
     * @param paramMap
     * @param request
     * @return
     */
    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request){


        try {
            boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名

            String trade_status = paramMap.get("trade_status");

            String out_trade_no = paramMap.get("out_trade_no");
            if(flag){

                if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){

                    PaymentInfo paymentInfoQuery = new PaymentInfo();
                    paymentInfoQuery.setOutTradeNo(out_trade_no);
                    PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);

                    if (paymentInfo.getPaymentStatus()==PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                        return "failure";
                    }

                    PaymentInfo paymentInfoUPD = new PaymentInfo();
                    paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                    paymentInfoUPD.setCreateTime(new Date());

                    paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);

                    return "success";
                }
            }else{

                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return "failure";
    }

    // payment.gmall.com/refund?orderId=100
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){

        boolean result = paymentService.refund(orderId);
        return ""+result;
    }


    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        System.out.println("************"+paymentInfo.getOrderId());
        paymentService.sendPaymentResult(paymentInfo,result);

        return "ok";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String  queryPaymentResult(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        return ""+flag;
    }
}

