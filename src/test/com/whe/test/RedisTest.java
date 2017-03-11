package com.whe.test;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Created by trustme on 2017/2/12.
 * Test
 */
public class RedisTest {
    public static void main(String[] args) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxTotal(20);
        poolConfig.setMinIdle(10);
        JedisPool jedisPool = new JedisPool(poolConfig, "172.16.63.104", 6379, 2000, "yhtest");

        long l = System.currentTimeMillis();
        Jedis jedis = jedisPool.getResource();
        Pipeline pipeline = jedis.pipelined();
        IntStream.rangeClosed(0,10000).forEach(i -> {
         //   jedis.del("key" + i);
            pipeline.lpush("list", "val" + i);
            System.out.println(i);
        });
        pipeline.sync();
        System.out.println("插入耗时:" + (System.currentTimeMillis() - l));
    }

    @Test
    public void clusterTest() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxTotal(20);
        poolConfig.setMinIdle(10);
        Set<HostAndPort> set = new HashSet<>();
        for (int i = 6385; i < 6386; i++) {
            set.add(new HostAndPort("192.168.88.128", i));
        }
        JedisCluster jedisCluster = new JedisCluster(set, poolConfig);
        IntStream.rangeClosed(0, 1000).forEach(i -> {
            // jedisCluster.del("key" + i);
             jedisCluster.lpush("list", "val" + i);
            System.out.println(i);
        });
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
        jedis.set("s","s");
        Pipeline pipeline = jedis.pipelined();
        for (int i = 0; i < 10000; i++) {
            pipeline.lpush("list","val"+i);
            System.out.println(i);
        }
        jedis.close();
        System.out.println("插入耗时:" + (System.currentTimeMillis() - l));
    }
}
