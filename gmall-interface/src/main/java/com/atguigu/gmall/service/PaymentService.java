package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    boolean refund(String orderId);

    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    boolean checkPayment(PaymentInfo paymentInfoQuery);

    public void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);
}
