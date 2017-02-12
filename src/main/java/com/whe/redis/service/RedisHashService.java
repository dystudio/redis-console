package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisHashService
 */
@Service
public class RedisHashService {
    private Set<String> hashKeys = new HashSet<>();

    /**
     * 获得所有hash数据
     *
     * @return map
     */
    public Map<String, Map<String, String>> getAllHash() {
        final Map<String, Map<String, String>> hashMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeHash(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            //检查集群节点是否发生变化
            RedisClusterUtils.checkClusterChange(jedisCluster);
            jedisCluster.getClusterNodes()
                    .forEach((key, pool) -> {
                        if (JedisFactory.getRedisClusterNode().getMasterNodeInfoSet().contains(key)) {
                            Jedis jedis = null;
                            try {
                                jedis = pool.getResource();
                                hashMap.putAll(getNodeHash(jedis));
                            } finally {
                                assert jedis != null;
                                jedis.close();
                            }
                        }
                    });
        }
        return hashMap;
    }

    /**
     * 模糊分页查询hash类型数据
     *
     * @return Page<Map<String, Map<String, String>>>
     */
    public Page<Map<String, Map<String, String>>> findHashPageByQuery(int pageNo, String pattern) {
        Page<Map<String, Map<String, String>>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            if (pageNo == 1) {
                Set<String> keys = jedis.keys(pattern);
                keys.forEach(key -> {
                    if (ServerConstant.REDIS_HASH.equalsIgnoreCase(jedis.type(key))) {
                        hashKeys.add(key);
                    }
                });
            }
            //总数据
            page.setTotalRecord(hashKeys.size());
            page.setPageNo(pageNo);
            Map<String, Map<String, String>> hashMap = findhashByKeys(hashKeys, jedis);
            page.setResults(hashMap);
            return page;
        }
        return null;
    }


    /**
     * 获得当前节点所有hash
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, Map<String, String>> getNodeHash(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_HASH.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, jedis::hgetAll));
    }


    /**
     * 根据key查找hash类型数据
     *
     * @param keys  Set<String> keys
     * @param jedis Jedis
     * @return Map<String, Map<String, String>>
     */
    private Map<String, Map<String, String>> findhashByKeys(Set<String> keys, Jedis jedis) {
        return keys.stream().collect(toMap(key -> key, jedis::hgetAll));
    }

    /**
     * 序列化保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHashSerialize(Map<String, Map<String, String>> hashMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
        }
    }


    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(Map<String, Map<String, String>> hashMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key, field, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key, field, val)));
        }
    }


}
