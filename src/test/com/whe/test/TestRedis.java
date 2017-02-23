package com.whe.test;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.stream.IntStream;

/**
 * Created by trustme on 2017/2/12.
 * Test
 */
public class TestRedis {
    public static void main(String[] args) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxTotal(20);
        poolConfig.setMinIdle(10);
        JedisPool jedisPool = new JedisPool(poolConfig, "192.168.200.134", 6381, 2000);

        long l = System.currentTimeMillis();

        IntStream.rangeClosed(0, 2000).parallel().forEach(i -> {
            Jedis jedis = jedisPool.getResource();
            jedis.select(1);
            jedis.sadd("set", "val" + i);
            // jedis.del("key" + i);
//            jedis.set("key" + i, "val" + i);
            System.out.println(i);
            jedis.close();
        });
        System.out.println("插入耗时:" + (System.currentTimeMillis() - l));
    }

    @Test
    public void test1() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxTotal(20);
        poolConfig.setMinIdle(10);
        JedisPool jedisPool = new JedisPool(poolConfig, "192.168.88.128", 6379, 2000);
        long l = System.currentTimeMillis();
        Jedis jedis = jedisPool.getResource();

        for (int i = 0; i < 100000; i++) {
            jedis.set("key" + i, "val" + i);
            // jedis.del("key"+i);
            System.out.println(i);
        }
        jedis.close();
        System.out.println("插入耗时:" + (System.currentTimeMillis() - l));
    }
}
