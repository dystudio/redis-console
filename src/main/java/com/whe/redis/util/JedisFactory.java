package com.whe.redis.util;

import com.whe.redis.cluster.ClusterInfoCache;
import com.whe.redis.domain.RedisInfo;
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
import java.util.concurrent.ConcurrentHashMap;
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
    private static String sentinel;
    private static ConcurrentHashMap<String, JedisPool> standAloneNode = new ConcurrentHashMap<>();

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
            try {
                standAlone = loadPro.getProperty("stand.alone");
                if (StringUtils.isNotBlank(standAlone)) {
                    String[] split = standAlone.split(":");

                    if (split.length == 2) {

                        String pass = loadPro.getProperty("stand.pass");
                        if (StringUtils.isNotBlank(pass)) {
                            jedisPool = new JedisPool(poolConfig, split[0], Integer.parseInt(split[1]), RedisPoolConfig.TIMEOUT, pass);
                        } else {
                            jedisPool = new JedisPool(poolConfig, split[0], Integer.parseInt(split[1]));
                        }
                        standAloneNode.put(standAlone, jedisPool);
                    }
                }
            } catch (Exception e) {
                log.error(standAlone + ":standAlone初始化错误->" + e.getMessage(), e);
            }

            //哨兵
            try {
                sentinel = loadPro.getProperty("sentinel");
                if (StringUtils.isNoneBlank(sentinel)) {
                    String masterName = loadPro.getProperty("master.name");
                    if (masterName == null) {
                        log.error("sentinel:masterName为空->");
                    }
                    Set<String> set = Stream.of(sentinel).map(str -> str.split(";")).flatMap(Arrays::stream).collect(Collectors.toSet());
                    String pass = loadPro.getProperty("sentinel.pass");
                    if (StringUtils.isNoneBlank(pass)) {
                        jedisSentinelPool = new JedisSentinelPool(masterName, set, poolConfig, pass);
                    } else {
                        jedisSentinelPool = new JedisSentinelPool(masterName, set, poolConfig);
                    }
                }
            } catch (Exception e) {
                log.error(sentinel + ":sentinel初始化错误->" + e.getMessage(), e);
            }

            //集群
            String redisCluster = loadPro.getProperty("cluster");
            try {
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
                    clusterInfoCache = new ClusterInfoCache(jedisCluster);
                }
            } catch (Exception e) {
                log.error(redisCluster + ":cluster初始化错误->" + e.getMessage(), e);
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

    public static JedisPool addStandAloneNode(RedisInfo redisInfo) {
        JedisPool jedisPool = null;
        if (redisInfo.getPassword() == null) {
            jedisPool = new JedisPool(RedisPoolConfig.getGenericObjectPoolConfig(), redisInfo.getHost(), redisInfo.getPort(), RedisPoolConfig.TIMEOUT);
        } else {
            jedisPool = new JedisPool(RedisPoolConfig.getGenericObjectPoolConfig(), redisInfo.getHost(), redisInfo.getPort(), RedisPoolConfig.TIMEOUT, redisInfo.getPassword());
        }
        return standAloneNode.put(redisInfo.getName(), jedisPool);
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

    public static String getSentinel() {
        return sentinel;
    }

    public static String getStandAlone() {
        return standAlone;
    }

    public static JedisSentinelPool getJedisSentinelPool() {
        return jedisSentinelPool;
    }
}

