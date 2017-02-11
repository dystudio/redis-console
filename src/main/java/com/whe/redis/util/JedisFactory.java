package com.whe.redis.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wang hongen on 2017/1/12.
 * Jedis工厂
 *
 * @author wanghongen
 */
public class JedisFactory {

    private static Jedis jedis;
    private static JedisCluster jedisCluster;
    private static RedisClusterNode redisClusterNode = null;

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
            String single = loadPro.getProperty("redis.single");
            String redisCluster = loadPro.getProperty("redis.cluster");
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
            } else {
                if (StringUtils.isNotBlank(redisCluster)) {
                    ServerConstant.REDIS_TYPE = ServerConstant.REDIS_CLUSTER;
                    Set<HostAndPort> set = Stream.of(redisCluster)
                            .map(str -> str.split(";"))
                            .flatMap(Arrays::stream)
                            .map(str -> str.split(":"))
                            .map(str -> new HostAndPort(str[0], Integer.parseInt(str[1])))
                            .collect(Collectors.toSet());
                    String pass = loadPro.getProperty("redis.pass");
                    if (StringUtils.isNotBlank(pass)) {
                        jedisCluster = new JedisCluster(set, 3000, 3000, 5, pass, new GenericObjectPoolConfig());
                    } else {
                        jedisCluster = new JedisCluster(set);
                    }

                    //保存集群节点信息
                    Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
                    for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
                        Jedis jedis = entry.getValue().getResource();
                        if ("PONG".equalsIgnoreCase(jedis.ping())) {
                            redisClusterNode = new RedisClusterNode(jedis.clusterNodes());
                            jedis.close();
                            break;
                        }
                    }
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

    public static RedisClusterNode getRedisClusterNode() {
        return redisClusterNode;
    }

    public static void setRedisClusterNode(RedisClusterNode redisClusterNode) {
        JedisFactory.redisClusterNode = redisClusterNode;
    }
}

