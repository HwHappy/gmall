package com.atguigu.gmall.config;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool = null;

    public void initJedisPool(String host,int port ,int database){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置参数   最大连接数
        jedisPoolConfig.setMaxTotal(100);
        //设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        //设置最小剩余数
        jedisPoolConfig.setMinIdle(10);
        //设置开机自检
        jedisPoolConfig.setTestOnBorrow(true);
        //如果达到最大连接池的时候，需要等待
        jedisPoolConfig.setBlockWhenExhausted(true);

      jedisPool = new JedisPool(jedisPoolConfig,host,port,20*1000);
    }

    public Jedis getJedis(){
        return jedisPool.getResource();
    }
}
