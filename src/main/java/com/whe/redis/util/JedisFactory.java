package com.whe.redis.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
    private static ClusterInfoCache clusterInfoCache = null;
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
            GenericObjectPoolConfig poolConfig = RedisPoolConfig.getGenericObjectPoolConfig();

            //单机
            standAlone = loadPro.getProperty("stand.alone");
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

            //哨兵
            String sentinel = loadPro.getProperty("sentinel");
            if (StringUtils.isNoneBlank(sentinel)) {
                String masterName = loadPro.getProperty("master.name");
                if (masterName == null) {
                    log.warn("sentinel:masterName为空->");
                }
                Set<String> set = Stream.of(sentinel).map(str -> str.split(";")).flatMap(Arrays::stream).collect(Collectors.toSet());
                String pass = loadPro.getProperty("sentinel.pass");
                if (StringUtils.isNoneBlank(pass)) {
                    jedisSentinelPool = new JedisSentinelPool(masterName, set, poolConfig, pass);
                } else {
                    jedisSentinelPool = new JedisSentinelPool(masterName, set, poolConfig);
                }
            }

            //集群
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
                clusterInfoCache=new ClusterInfoCache(jedisCluster);
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

    public static ClusterInfoCache getClusterInfoCache() {
        return clusterInfoCache;
    }

    public static void setClusterInfoCache(ClusterInfoCache clusterInfoCache) {
        JedisFactory.clusterInfoCache = clusterInfoCache;
    }

    public static String getStandAlone() {
        return standAlone;
    }

    public static JedisSentinelPool getJedisSentinelPool() {
        return jedisSentinelPool;
    }
}

