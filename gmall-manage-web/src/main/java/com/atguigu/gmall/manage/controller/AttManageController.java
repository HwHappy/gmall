package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttManageController {


    @Reference
    ListService listService;
    @Reference
    ManageService manageService;

    @RequestMapping("onSale")
    public String onSale(String skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();


        BeanUtils.copyProperties(skuInfo,skuLsInfo);


        listService.saveSkuLsInfo(skuLsInfo);


        return "ok";

    }

}
