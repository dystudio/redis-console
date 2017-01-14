package com.whe.redis.util;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by wang hongen on 2017/1/12.
 * Redis工厂
 *
 * @author wanghongen
 */
public class JedisFactory {

    private static Jedis jedis;
    private static JedisCluster jedisCluster;

    public static void init() {
        System.out.println("初始化数据");
        /*
         * 信息载入
         */
        Properties loadPro = new Properties();
        InputStream in = JedisFactory.class.getResourceAsStream("/redis.properties");
        try {
            loadPro.load(in);
            String redisType = loadPro.getProperty("redis.type");

            String ipAndPort = loadPro.getProperty("redis");
            if (ServerConstant.STAND_ALONE.equalsIgnoreCase(redisType)) {
                ServerConstant.REDIS_TYPE = ServerConstant.STAND_ALONE;
                String[] split = ipAndPort.split(":");
                if (split.length != 2) {
                    throw new RuntimeException("ip和端口格式不正确");
                }
                jedis = new Jedis(split[0], Integer.parseInt(split[1]));
                String pass = loadPro.getProperty("redis.pass");
                if (StringUtils.isNotBlank(pass)) {
                    jedis.auth(pass);
                }
            } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(redisType)) {
                ServerConstant.REDIS_TYPE = ServerConstant.REDIS_CLUSTER;
                String[] split = ipAndPort.split(";");
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

    public static JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public static Jedis getJedis() {
        return jedis;
    }
}

