package com.atguigu.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.util.JwtUtil;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Reference
    UserService userService;

    @Value("${token.key}")
    String signKey;


    @RequestMapping("index")
    //@ResponseBody
    public String index(HttpServletRequest request){

        String originUrl =request.getParameter("originUrl");

        System.out.println("originUrl="+originUrl);


        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){
        //从request中获取nginx的配置文件中配置的IP地址
        String salt = request.getHeader("X-forwarded-for");

        if (userInfo !=null){
            //从前端获取的用户名和密码封装到userInfo，userInfo不为空进行登录
            UserInfo info = userService.login(userInfo);
            if (info ==null){
                return "fail";
            }else{
                //生成token
                Map<String ,Object> map = new HashMap();
                map.put("userId",info.getId());
                map.put("nickName",info.getNickName());
                String token = JwtUtil.encode(signKey, map, salt);

                return token;
            }
        }
        return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){

        String token = request.getParameter("token");
       // String currentIp = request.getParameter("currentIp");
        String salt = request.getHeader("X-forwarded-for");

        Map<String, Object> map = JwtUtil.decode(token, signKey, salt);
        if (map !=null){
            String userId = (String) map.get("userId");

            UserInfo userInfo = userService.verify(userId);
            if (userInfo !=null){
                return "success";
            }
        }

        return "fail";

    }











}
