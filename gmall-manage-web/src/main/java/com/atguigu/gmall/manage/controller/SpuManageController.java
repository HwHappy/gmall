package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {
    @Reference
    ManageService manageService;


    /**
     *获取spu属性列表
     * @param spuInfo
     * @return
     */
    //http://localhost:8082/spuList?catalog3Id=61
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){

       return manageService.spuList(spuInfo);
    }

    /**
     * 获取销售属性列表
     * @return
     */
    //http://localhost:8082/baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    public  List<BaseSaleAttr> baseSaleAttrList(){
      return   manageService.baseSaleAttrList();


    }

    /**
     * 保存Spu
     */
    //http://localhost:8082/saveSpuInfo
    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){

        manageService.saveSpuInfo(spuInfo);
    }





}
