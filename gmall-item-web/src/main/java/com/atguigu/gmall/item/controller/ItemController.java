package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.LoginRequire;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ItemController {
    @Reference
    ListService listService;

    @Reference
    ManageService manageService;
    @RequestMapping("{skuId}.html")
  //  @LoginRequire
    public String skuInfoPage(
            @PathVariable(value = "skuId") String skuId, HttpServletRequest request){
        System.out.println("商品Id："+skuId);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        //销售属性回显
       List<SpuSaleAttr> spuSaleAttrList =   manageService.getSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());

       //商品根据销售属性绑定跳转
       List<SkuSaleAttrValue> skuSaleAttrValueList =  manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
       String key = "";
       Map<String,String> map = new HashMap<>();
       if (skuSaleAttrValueList != null){
           for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
               SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
               if (key.length()>0){
                   key+="|";
               }
               key+=skuSaleAttrValue.getSaleAttrValueId();
               if((i+1)==skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                   map.put(key,skuSaleAttrValue.getSkuId());
                   key="";
               }
           }
       }
        String valuesSkuJson = JSON.toJSONString(map);
       request.setAttribute("valuesSkuJson",valuesSkuJson);


        request.setAttribute("spuSaleAttrList",spuSaleAttrList);
        request.setAttribute("skuInfo",skuInfo);
        listService.incrHotScore(skuId);
        return "item";
    }
}
