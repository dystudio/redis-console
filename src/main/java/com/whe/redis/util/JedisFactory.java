package com.whe.redis.util;

import com.whe.redis.cluster.ClusterInfoCache;
import com.whe.redis.conf.RedisConf;
import com.whe.redis.conf.RedisPoolConfig;
import com.whe.redis.domain.RedisInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.whe.redis.util.ServerConstant.*;

/**
 * Created by wang hongen on 2017/1/12.
 * Jedis工厂
 *
 * @author wanghongen
 */
public class JedisFactory {
    private static final Logger log = LoggerFactory.getLogger(JedisFactory.class);
    private static final RedisConf redisConf = RedisConf.getInstance();
    private final static GenericObjectPoolConfig poolConfig = RedisPoolConfig.getGenericObjectPoolConfig();

    private static Map<String, JedisPool> standaloneMap = Collections.synchronizedMap(new LinkedHashMap<String, JedisPool>());
    private static Map<String, JedisSentinelPool> sentinelMap = Collections.synchronizedMap(new LinkedHashMap<String, JedisSentinelPool>());
    private static Map<String, JedisCluster> clusterMap = Collections.synchronizedMap(new LinkedHashMap<String, JedisCluster>());
    private static ConcurrentHashMap<String, ClusterInfoCache> clusterInfoCacheMap = new ConcurrentHashMap<>();

    /**
     * 初始化数据
     */
    public static void init() {
        Map<Object, Object> objectObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());
        log.info("初始化数据");
        try {
            Optional<Map<String, List<RedisInfo>>> mapOptional = redisConf.readConf();
            mapOptional.ifPresent(infoMap -> {
                List<RedisInfo> redisInfoList = null;
                try {
                    //单机
                    redisInfoList = infoMap.get(STANDALONE);
                    if (redisInfoList != null && redisInfoList.size() > 0) {
                        redisInfoList.forEach(JedisFactory::addStandAlone);
                    }
                } catch (Exception e) {
                    log.error(redisInfoList + ":standAlone初始化错误->" + e.getMessage(), e);
                }
                try {
                    //哨兵
                    redisInfoList = infoMap.get(SENTINEL);
                    if (redisInfoList != null && redisInfoList.size() > 0) {
                        redisInfoList.forEach(JedisFactory::addSentinel);
                    }
                } catch (Exception e) {
                    log.error(redisInfoList + ":sentinel初始化错误->" + e.getMessage(), e);
                }
                try {

                    //集群
                    redisInfoList = infoMap.get(CLUSTER);
                    if (redisInfoList != null && redisInfoList.size() > 0) {
                        redisInfoList.forEach(JedisFactory::addCluster);
                    }
                } catch (Exception e) {
                    log.error(redisInfoList + ":cluster初始化错误->" + e.getMessage(), e);
                }
            });
            //保存集群节点信息
            // clusterInfoCache = new ClusterInfoCache(jedisCluster);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化错误:" + e.getMessage(), e);
        }
    }

    /**
     * 单机类型 添加新redis服务
     *
     * @param redisInfo
     * @return
     */
    public static boolean addStandAlone(RedisInfo redisInfo) {
        JedisPool jedisPool;
        if (StringUtils.isBlank(redisInfo.getPassword())) {
            jedisPool = new JedisPool(poolConfig, redisInfo.getHost(), redisInfo.getPort(), RedisPoolConfig.TIMEOUT);
        } else {
            jedisPool = new JedisPool(poolConfig, redisInfo.getHost(), redisInfo.getPort(), RedisPoolConfig.TIMEOUT, redisInfo.getPassword());
        }
        try (Jedis resource = jedisPool.getResource()) {
            String pong = "PONG";
            boolean ping = pong.equalsIgnoreCase(resource.ping());
            standaloneMap.put(redisInfo.getName(), jedisPool);
            return ping;
        }
    }

    public static boolean addSentinel(RedisInfo redisInfo) {
        JedisSentinelPool jedisSentinelPool;
        Set<String> set = new HashSet<>();
        set.add(redisInfo.getHost() + ":" + redisInfo.getPort());
        if (StringUtils.isBlank(redisInfo.getPassword())) {
            jedisSentinelPool = new JedisSentinelPool(redisInfo.getName(), set, poolConfig);
        } else {
            jedisSentinelPool = new JedisSentinelPool(redisInfo.getName(), set, poolConfig,redisInfo.getPassword());
        }
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            String pong = "PONG";
            boolean ping = pong.equalsIgnoreCase(jedis.ping());
            sentinelMap.put(redisInfo.getName(), jedisSentinelPool);
            return ping;
        }
    }

    public static boolean addCluster(RedisInfo redisInfo) {
        JedisCluster jedisCluster;
        Set<HostAndPort> set = new HashSet<>();
        set.add(new HostAndPort(redisInfo.getHost(), redisInfo.getPort()));
        if (StringUtils.isBlank(redisInfo.getPassword())) {
            jedisCluster = new JedisCluster(set, poolConfig);
        } else {
            jedisCluster = new JedisCluster(set, RedisPoolConfig.TIMEOUT, RedisPoolConfig.TIMEOUT, RedisPoolConfig.MAX_ATTEMPTS, redisInfo.getPassword(), poolConfig);
        }
        jedisCluster.exists("key");
        clusterMap.put(redisInfo.getName(), jedisCluster);
        clusterInfoCacheMap.put(redisInfo.getName(), new ClusterInfoCache(jedisCluster));
        return true;
    }

    /**
     * 关闭连接
     */
    public static void close() {
        if (standaloneMap != null) standaloneMap.forEach((name, pool) -> pool.close());
        if (sentinelMap != null) sentinelMap.forEach((name, pool) -> pool.close());
        if (clusterMap != null) clusterMap.forEach((name, cluster) -> {
            try {
                cluster.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 获得jedisCluster
     *
     * @return JedisCluster
     */
    public static JedisCluster getJedisCluster() {
        Collection<JedisCluster> values = clusterMap.values();
        for (JedisCluster jedisCluster : values) {
            return jedisCluster;
        }
        return null;
    }

    /**
     * 获得JedisPool
     *
     * @return JedisPool
     */
    public static JedisPool getJedisPool(String name) {
        return standaloneMap.get(name);
    }

    public static JedisSentinelPool getJedisSentinelPool() {
        for (JedisSentinelPool jedisSentinelPool : sentinelMap.values()) {
            return jedisSentinelPool;
        }
        return null;
    }

    public static ClusterInfoCache getClusterInfoCache() {
        for (ClusterInfoCache clusterInfoCache : clusterInfoCacheMap.values()) {
            return clusterInfoCache;
        }
        return null;
    }

    public static Map<String, JedisPool> getStandaloneMap() {
        return standaloneMap;
    }

    public static Map<String, JedisSentinelPool> getSentinelMap() {
        return sentinelMap;
    }

    public static Map<String, JedisCluster> getClusterMap() {
        return clusterMap;
    }
}

