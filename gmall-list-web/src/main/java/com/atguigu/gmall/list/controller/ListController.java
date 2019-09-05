package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.LoginRequire;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;



    @RequestMapping("list.html")
    @LoginRequire
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){
        //重新设置分页显示的条数
        skuLsParams.setPageSize(2);
        //显示商品信息
        SkuLsResult search = listService.search(skuLsParams);
        List<SkuLsInfo> skuLsInfoList = search.getSkuLsInfoList();




        //用于显示平台属性信息
        List<String> attrValueIdList = search.getAttrValueIdList();
       List<BaseAttrInfo>  attrList =manageService.getAttrList(attrValueIdList);

       //面包屑
        ArrayList<BaseAttrValue> baseAttrValueList = new ArrayList<>();
        //拼接url参数的方法
       String urlParam =makeUrlParam(skuLsParams);
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()) {
                        if (valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            //面包屑
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);

                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValueList.add(baseAttrValueSelected);


                        }
                    }
                }
            }

        }

        //把分页所需要的数据放到域中
        request.setAttribute("totalPage",search.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());


        request.setAttribute("baseAttrValueList",baseAttrValueList);
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("urlParam",urlParam);
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        request.setAttribute("attrList",attrList);

        //  return JSON.toJSONString(search);
        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {
        String urlParam = "";
        ArrayList<String> paramList = new ArrayList<>();

        //拼接搜索关键字
        if (skuLsParams.getKeyword()!= null){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        //拼接三级分类id
        if(skuLsParams.getCatalog3Id()!= null){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        //拼接属性列表参数
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
//                if (urlParam.length()>0){
//                    urlParam+="&";
//                }
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        continue;
                    }
                }
                if (urlParam.length() >0){
                    urlParam+="&";
                }

                urlParam+="valueId="+valueId;

            }
        }
        return urlParam;

    }
}
