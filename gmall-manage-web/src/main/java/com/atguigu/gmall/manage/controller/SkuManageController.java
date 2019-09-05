package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {
    @Reference
    ManageService manageService;

    //http://localhost:8082/spuImageList?spuId=59

    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(String spuId){

       return manageService.spuImageList(spuId);

    }
     //http://localhost:8082/spuSaleAttrList?spuId=59
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        return manageService.spuSaleAttrList(spuId);
    }
    //http://localhost:8082/saveSkuInfo
    @RequestMapping("saveSkuInfo")
   public void  saveSkuInfo(@RequestBody SkuInfo skuInfo){
        if (skuInfo != null){
            manageService.saveSkuInfo(skuInfo);
        }


    }
}
