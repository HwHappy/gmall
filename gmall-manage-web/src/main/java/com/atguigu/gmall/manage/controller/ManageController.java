package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@CrossOrigin
@Controller
public class ManageController {
    @Reference
    ManageService manageService;


    @RequestMapping("index")
    public String index(){
        return "index";
    }

    //http://localhost:8082/getCatalog1
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
       return  manageService.getCatalog1();

    }
//http://localhost:8082/getCatalog2?catalog1Id=2
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){

        return  manageService.getCatalog2(catalog1Id);


    }
//    http://localhost:8082/getCatalog3?catalog2Id=18
    @RequestMapping("getCatalog3")
    @ResponseBody
    public  List<BaseCatalog3> getCatalog3(String catalog2Id){
        return  manageService.getCatalog3(catalog2Id);
    }
//http://localhost:8082/attrInfoList?catalog3Id=4
    @RequestMapping("attrInfoList")
    @ResponseBody
    public  List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return  manageService.attrInfoList(catalog3Id);

    }
//http://localhost:8082/saveAttrInfo
    /**
     * 添加平台属性
     *
     */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        manageService.saveAttrInfo(baseAttrInfo);
    }
    /**
     * 修改，获取对应的平台属性及属性值进行修改
     */
    //http://localhost:8082/getAttrValueList?attrId=108
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public  List<BaseAttrValue>getAttrValueList(String attrId){

       return  manageService.getAttrValueList(attrId);

    }

}
