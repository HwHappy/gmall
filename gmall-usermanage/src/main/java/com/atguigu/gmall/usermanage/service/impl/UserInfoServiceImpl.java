package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> fillAll() {
        return userInfoMapper.selectAll();
    }

 /*   @Override
    public void insertUserInfo(UserInfo userInfo) {

    }

    @Override
    public void updateUserInfo(UserInfo userInfo) {

    }*/

    @Override
    public List<UserAddress> getAddressList(String userId) {
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);
        return  userAddressMapper.selectByExample(example);

    }

    /**
     * 登录的方法
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        //对密码进行加密
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());

        userInfo.setPasswd(password);
        //到数据库中去获取对象
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info != null){
            //保存在redis缓存中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();

            return info;

        }

            return null;
    }

    /**
     * 验证的方法
     * @param userId
     * @return
     */
    @Override
    public UserInfo verify(String userId){

        Jedis jedis = redisUtil.getJedis();
        String key = userKey_prefix+userId+userinfoKey_suffix;

        String userJson = jedis.get(key);

        jedis.expire(key ,userKey_timeOut);
       // jedis.close();

        if (userJson != null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);

            return userInfo;
        }

        return null;

    }
    
    
    
    
    
    
}
