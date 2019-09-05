package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

import java.util.List;

public interface UserService {

    List<UserInfo>  fillAll();

   // void insertUserInfo(UserInfo userInfo);

  //  void updateUserInfo(UserInfo userInfo);

    List<UserAddress> getAddressList(String userId);

    UserInfo login(UserInfo userInfo);

    UserInfo verify(String userId);
}
