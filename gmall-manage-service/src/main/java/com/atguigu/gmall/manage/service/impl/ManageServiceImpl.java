package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.manage.constant.ManageConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service. ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;


import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl  implements ManageService{

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    private  RedisUtil redisUtil;

    /**
     * 获取一级分类
     * @return
     */
    @Override
    public List<BaseCatalog1> getCatalog1() {
       return   baseCatalog1Mapper.selectAll();

    }

    /**
     * 获取二级分类
     * @param catalog1Id
     * @return
     */
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
       return  baseCatalog2Mapper.select(baseCatalog2);
    }

    /**
     * 获取三级分类
     * @param catalog2Id
     * @return
     */
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    /**
     * 获取平台属性
     * @param catalog3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);

      //  return baseAttrInfoMapper.select(baseAttrInfo) ;


        return baseAttrInfoMapper.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }

    /**
     * 保存平台属性及属性值或修改某属性的属性值
     * @param baseAttrInfo
     */
    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //判断保存或者修改
        if (baseAttrInfo.getId() !=null && baseAttrInfo.getId().length() >0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            //保存平台属性
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }



        //删除之前保存的属性值再重新保存
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);
        //保存平台属性值

        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        if(baseAttrValueList != null && baseAttrValueList.size() > 0){
            for (int i = 0; i < baseAttrValueList.size(); i++) {
               BaseAttrValue baseAttrValue = baseAttrValueList.get(i);
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }

    }

    /**
     * 根据属性attrId获取属性值的列表用于回显数据
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public List<SpuInfo> spuList(SpuInfo spuInfo) {


        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }


    /**
     * 保存Spu
     * @param spuInfo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insertSelective(spuInfo);
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
    }

    /**
     * 获取spu图片列表
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    /**
     * 获取销售属性列表
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //skuInfo表

        skuInfoMapper.insertSelective(skuInfo);

        //SkuAttrValue表
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        //SkuSaleAttrValue表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }

        //SkuImage表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList!= null && skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);

            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo = null;
        RLock lock = null;
        Jedis jedis =null;
         try {
             //创建配置类的实例对象，设置连接redis的地址
             Config config = new Config();
             config.useSingleServer().setAddress("redis://192.168.233.129:6379");
             RedissonClient redissonClient = Redisson.create(config);
             //
             lock = redissonClient.getLock("my-lock");

             lock.lock(10, TimeUnit.SECONDS);
             jedis = redisUtil.getJedis();
             String userKey  = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
             if (jedis.exists(userKey)){
                 String userJson = jedis.get(userKey);
                 if (!StringUtils.isEmpty(userJson)){
                     skuInfo  = JSON.parseObject(userJson, SkuInfo.class);
                     return skuInfo;
                 }
             }else {
                 skuInfo  = getSkuInfoByDB(skuId);
                 jedis.setex(userKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));

                 return skuInfo;
             }
         } catch (Exception e) {
               e.printStackTrace();
              }finally {
             if (jedis!=null){
                 jedis.close();
             }
             if (lock!=null){
                 lock.unlock();
             }

         }

        return getSkuInfoByDB(skuId);

    }

    private SkuInfo getSkuInfoJiasuo(String skuId) {
        SkuInfo skuInfo =null;
        Jedis jedis =null;
        try {


            jedis =   redisUtil.getJedis();
            //  定义key 获取数据
            String userKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            String skuJson = jedis.get(userKey);

            if (skuJson==null){
                System.out.println("缓存没有数据数据");
                // set k1 v1 px 10000 nx
                // 定义锁的Key sku:skuId:lock
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 执行命令
                String lockKey  = jedis.set(skuLockKey, "ATGUIGU", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){
                    System.out.println("获取到分布式锁！");
                    System.out.println("从数据库中取数据");
                    skuInfo = getSkuInfoByDB(skuId);
                    System.out.println("把取到的数据放在缓存redis中");
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(userKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                    //删除锁
                    jedis.del(skuLockKey);

                    return skuInfo;
                }else {

                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }
            }else {

                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
        }

        return getSkuInfoByDB(skuId);
    }

    /**
     * to'm
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoByDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);

        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String skuId, String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {


        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");

       List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);

        return baseAttrInfoList;
    }


}
