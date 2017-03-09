package com.whe.redis.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

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
    private static final Logger log = LoggerFactory.getLogger(JedisFactory.class);

    private static JedisPool jedisPool;
    private static JedisSentinelPool jedisSentinelPool;
    private static JedisCluster jedisCluster;
    private static ClusterNode clusterNode = null;
    private static String standAlone;

    /**
     * 初始化数据
     */
    public static void init() {
        log.info("初始化数据");
        /*
         * 信息载入
         */
        Properties loadPro = new Properties();
        InputStream in = JedisFactory.class.getResourceAsStream("/redis.properties");
        try {
            loadPro.load(in);
            standAlone = loadPro.getProperty("stand.alone");
            GenericObjectPoolConfig poolConfig = RedisPoolConfig.getGenericObjectPoolConfig();
            if (StringUtils.isNotBlank(standAlone)) {
                ServerConstant.REDIS_TYPE = ServerConstant.STAND_ALONE;
                String[] split = standAlone.split(":");
                if (split.length != 2) {
                    log.warn("standAlone:ip和端口格式不正确->" + standAlone);
                    throw new RuntimeException("ip和端口格式不正确");
                }

                String pass = loadPro.getProperty("stand.pass");
                if (StringUtils.isNotBlank(pass)) {
                    jedisPool = new JedisPool(poolConfig, split[0], Integer.parseInt(split[1]), RedisPoolConfig.TIMEOUT, pass);
                } else {
                    jedisPool = new JedisPool(poolConfig, split[0], Integer.parseInt(split[1]));
                }
            }

            String sentinel = loadPro.getProperty("sentinel");
            if (StringUtils.isNoneBlank(sentinel)) {
                String masterName = loadPro.getProperty("master.name");
                if (masterName == null) {
                    log.warn("sentinel:masterName为空->");
                }
                Set<String> set = Stream.of(sentinel).map(str -> str.split(";")).flatMap(Arrays::stream).collect(Collectors.toSet());
                String pass = loadPro.getProperty("sentinel.pass");
                if (StringUtils.isNoneBlank(pass)) {
                    jedisSentinelPool = new JedisSentinelPool(masterName, set, RedisPoolConfig.getGenericObjectPoolConfig(), pass);
                } else {
                    jedisSentinelPool = new JedisSentinelPool(masterName, set, RedisPoolConfig.getGenericObjectPoolConfig());
                }
            }

            String redisCluster = loadPro.getProperty("cluster");

            if (StringUtils.isNotBlank(redisCluster)) {
                ServerConstant.REDIS_TYPE = ServerConstant.REDIS_CLUSTER;

                Set<HostAndPort> set = Stream.of(redisCluster).map(str -> str.split(";")).flatMap(Arrays::stream).map(str -> str.split(":")).map(str -> new HostAndPort(str[0], Integer.parseInt(str[1]))).collect(Collectors.toSet());
                String pass = loadPro.getProperty("cluster.pass");
                if (StringUtils.isNotBlank(pass)) {
                    jedisCluster = new JedisCluster(set, RedisPoolConfig.TIMEOUT, RedisPoolConfig.TIMEOUT, RedisPoolConfig.MAX_ATTEMPTS, pass, poolConfig);
                } else {
                    jedisCluster = new JedisCluster(set, poolConfig);
                }
                //保存集群节点信息
                Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
                for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
                    Jedis jedis = null;
                    try {
                        jedis = entry.getValue().getResource();
                        clusterNode = new ClusterNode(jedis.getClient().getHost() + ":" + jedis.getClient().getPort(), jedis.clusterNodes());
                        break;
                    } catch (Exception ignored) {
                        // try next nodes
                    } finally {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化错误:" + e.getMessage(), e);
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
        if (jedisPool != null) jedisPool.close();

        if (jedisCluster != null) try {
            jedisCluster.close();
        } catch (Exception e) {
            System.out.println("redisCluster关闭失败");
            e.printStackTrace();
        }
        if (jedisSentinelPool != null) {
            jedisSentinelPool.close();
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
     * 获得JedisPool
     *
     * @return JedisPool
     */
    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static ClusterNode getClusterNode() {
        return clusterNode;
    }

    public static void setClusterNode(ClusterNode clusterNode) {
        JedisFactory.clusterNode = clusterNode;
    }

    public static String getStandAlone() {
        return standAlone;
    }

    public static JedisSentinelPool getJedisSentinelPool() {
        return jedisSentinelPool;
    }
}

