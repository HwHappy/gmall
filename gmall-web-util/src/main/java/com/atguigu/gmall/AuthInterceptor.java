package com.atguigu.gmall;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");
        if (token !=null){
            //如果token不为空，就把token保存到cookie中
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }

        if (token ==null){
            //token为空就从cookie中去获取
          token =  CookieUtil.getCookieValue(request,"token",false);
        }

        if (token!= null){
            //token不为空就去获取存有用户信息的map
          Map map =   getUserMapByToken(token);
          //把获取到的nickName放到request中
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName",nickName);
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取标有LoginRequire的方法
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation !=null) {
            //标有LoginRequire的方法
            String remoteAddr = request.getHeader("X-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            if ("success".equals(result)) {
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);
                return true;
            } else {
                if (methodAnnotation.autoRedirect()) {
                    //没登录就跳到登录页面
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeURL);
                    return false;

                }

            }
        }

        return true;
    }

    /**
     *通过token获取Map的方法
     * @param token
     * @return
     */
    private Map getUserMapByToken(String token) {

        String tokenUserInfo = StringUtils.substringBetween(token,".");

        System.out.println("tokenUserInfo="+tokenUserInfo);


        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);

        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("tokenJson"+tokenJson);

        Map map = JSON.parseObject(tokenJson, Map.class);

        return map;


    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    }
}
