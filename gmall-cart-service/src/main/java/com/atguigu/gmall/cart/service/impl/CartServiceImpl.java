package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.config.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;
    @Autowired
    RedisUtil redisUtil;


    /**
     * 用户已经登录，购物车的添加
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //  先查cart中是否对应的购物项
          CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoExist != null){
            //更新购物车中商品的数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //更新实时价格
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        } else{
            // 如果不存在，保存购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            //设置购物项的属性值
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            //保存购物项到数据库中
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;

        }
        String   cartKey =   CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();

     jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));
     //更新购物车的过期时间
        String userKey    =CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        //获取redis中用户信息的过期时间
        Long ttl = jedis.ttl(userKey);
        //设置购物车的过期时间和用户的过期时间一致
        jedis.expire(cartKey,ttl.intValue());
        //关闭jedis
        jedis.close();


    }

    /**
     * 获取购物车列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String  cartKey =   CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartJsons = jedis.hvals(cartKey);

        if (cartJsons != null && cartJsons.size()>0){
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });

            return cartInfoList;
        }else {
           return LoadCartCache(userId);
        }


    }

    /**
     * 合并购物车
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        for (CartInfo cartInfoCK : cartListFromCookie) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch =true;
                }

            }
            if (!isMatch){
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }

        List<CartInfo> cartInfoList = LoadCartCache(userId);
        for (CartInfo cartInfo : cartInfoList) {
            for (CartInfo info : cartListFromCookie) {
                if (cartInfo.getSkuId().equals("1")){
                    cartInfo.setIsChecked(info.getIsChecked());

                    checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                }
            }
        }

        return cartInfoList;
    }

    /**
     * 检查购物项是否被选中
     * @param skuId
     * @param ischecked
     * @param userId
     */
    @Override
    public void checkCart(String skuId, String ischecked, String userId) {

        Jedis jedis = redisUtil.getJedis();
        //取得购物车中的信息
       String cartKey =  CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(cartKey, skuId);

        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

        cartInfo.setIsChecked(ischecked);

        String cartCheckedJson = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey,skuId,cartCheckedJson);

        //新增到已选的数据库中
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(ischecked)){
            jedis.hset(userCheckedKey,skuId,cartCheckedJson);
        }else {
            jedis.hdel(userCheckedKey,skuId);
        }

        jedis.close();
    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        ArrayList<CartInfo> cartInfoList = new ArrayList<>();
        //取得购物车中的信息
        String cartKey =  CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        List<String> cartJsonList = jedis.hvals(cartKey);
        if (cartJsonList != null && cartJsonList.size()>0){
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

                    cartInfoList.add(cartInfo);

            }
        }


        return cartInfoList;

    }

    /**
     * 当缓存和cookie中没数据时从数据库中获取数据
     * @param userId
     * @return
     */
    private List<CartInfo> LoadCartCache(String userId) {
        //从数据库中获取购物车的数据
      List<CartInfo> cartInfoList =  cartInfoMapper.selectCartListWithCurPrice(userId);
      //数据为空返回null
      if (cartInfoList==null || cartInfoList.size() ==0){
          return null;
      }
        //定义redis中的key
      String cartKey =  CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        //创建存储数据的map
        HashMap<String, String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            //循环把对象转成json串
            String cartInfoJson = JSON.toJSONString(cartInfo);
            map.put(cartInfo.getSkuId(),cartInfoJson);
        }
        //把数据放入redis中
        jedis.hmset(cartKey,map);
        jedis.close();
        return cartInfoList;


    }
}
