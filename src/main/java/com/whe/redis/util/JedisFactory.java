package com.whe.redis.util;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by wang hongen on 2017/1/12.
 * Jedis工厂
 *
 * @author wanghongen
 */
public class JedisFactory {

    private static Jedis jedis;
    private static JedisCluster jedisCluster;

    /**
     * 初始化数据
     */
    public static void init() {
        System.out.println("初始化数据");
        /*
         * 信息载入
         */
        Properties loadPro = new Properties();
        InputStream in = JedisFactory.class.getResourceAsStream("/redis.properties");
        try {
            loadPro.load(in);
            String redisCluster = loadPro.getProperty("redis.cluster");
            String single = loadPro.getProperty("redis.single");
            if (StringUtils.isNotBlank(single)) {
                ServerConstant.REDIS_TYPE = ServerConstant.SINGLE;
                String[] split = single.split(":");
                if (split.length != 2) {
                    throw new RuntimeException("ip和端口格式不正确");
                }
                jedis = new Jedis(split[0], Integer.parseInt(split[1]));
                String pass = loadPro.getProperty("redis.pass");
                if (StringUtils.isNotBlank(pass)) {
                    jedis.auth(pass);
                }
            } else if (StringUtils.isNotBlank(redisCluster)) {
                ServerConstant.REDIS_TYPE = ServerConstant.REDIS_CLUSTER;
                String[] split = redisCluster.split(";");
                   /* Stream<String[]> stream = Arrays.stream(split).map(":"::split);
                    Set<HostAndPort> collect = stream.map(str -> new HostAndPort(str[0], Integer.parseInt(str[1]))).collect(Collectors.toSet());
                    System.out.println(collect);*/
                Set<HostAndPort> set = new HashSet<>();
                for (String str : split) {
                    String[] strArr = str.split(":");
                    if (strArr.length != 2) {
                        throw new RuntimeException("ip和端口格式不正确");
                    }
                    HostAndPort hostAndPort = new HostAndPort(strArr[0], Integer.parseInt(strArr[1]));
                    set.add(hostAndPort);
                }
                jedisCluster = new JedisCluster(set);
                String pass = loadPro.getProperty("redis.pass");
                if (StringUtils.isNotBlank(pass)) {
                    jedisCluster.auth(pass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 关闭连接
     */
    public static void close() {
        if (jedis != null) jedis.close();

        if (jedisCluster != null) try {
            jedisCluster.close();
        } catch (Exception e) {
            System.out.println("jedisCluster关闭失败");
            e.printStackTrace();
        }
    }

    /**
     * 获得jedisCluster
     *
     * @return JedisCluster
     */
    public static JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    /**
     * 获得jedis
     *
     * @return Jedis
     */
    public static Jedis getJedis() {
        return jedis;
    }
}

