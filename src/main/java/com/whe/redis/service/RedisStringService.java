package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisStringService
 */
@Service
public class RedisStringService {
    private Set<String> stringKeys = new HashSet<>();

    /**
     * 获得所有string类型数据
     *
     * @return Map<String, String>
     */
    public Map<String, String> getAllString(String pattern) {
        final Map<String, String> allString = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeString(JedisFactory.getJedis(), pattern);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            //检查集群节点是否发生变化
            RedisClusterUtils.checkClusterChange(jedisCluster);
            jedisCluster.getClusterNodes()
                    .entrySet()
                    .stream()
                    .filter(entry -> JedisFactory.getRedisClusterNode().getMasterNodeInfoSet().contains(entry.getKey()))
                    .forEach(entry -> {
                        if (JedisFactory.getRedisClusterNode().getMasterNodeInfoSet().contains(entry.getKey())) {
                            Jedis jedis = null;
                            try {
                                jedis = entry.getValue().getResource();
                                allString.putAll(getNodeString(jedis, pattern));
                            } finally {
                                assert jedis != null;
                                jedis.close();
                            }
                        }
                    });
        }
        return allString;
    }

    /**
     * 模糊分页查询string类型数据
     *
     * @return Page<Map<String, String>>
     */
    public Page<Map<String, String>> findStringPageByQuery(int pageNo, String pattern) {
        if(pattern==null){
            pattern="*";
        }
        Page<Map<String, String>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            if (pageNo == 1) {
                Set<String> keys = jedis.keys(pattern);
                keys.forEach(key -> {
                    if (ServerConstant.REDIS_STRING.equalsIgnoreCase(jedis.type(key))) {
                        stringKeys.add(key);
                    }
                });
            }
            //总数据
            page.setTotalRecord(stringKeys.size());
            page.setPageNo(pageNo);
            Map<String, String> stringMap = findStringByKeys(stringKeys, jedis);
            page.setResults(stringMap);
            return page;
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            //检查集群节点是否发生变化
            RedisClusterUtils.checkClusterChange(jedisCluster);
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            String finalPattern = pattern;
            clusterNodes.entrySet().forEach(pool -> {
                Jedis resource = pool.getValue().getResource();
                Set<String> keys = resource.keys(finalPattern);
                keys.forEach(key -> {
                    if (ServerConstant.REDIS_STRING.equalsIgnoreCase(resource.type(key))) {
                        stringKeys.add(key);
                    }
                });
                resource.close();
            });
            //总数据
            page.setTotalRecord(stringKeys.size());
            page.setPageNo(pageNo);
            clusterNodes.entrySet()
                    .stream()
                    .filter(entry -> JedisFactory.getRedisClusterNode().getMasterNodeInfoSet().contains(entry.getKey()))
                    .forEach(entry -> {
                        if (JedisFactory.getRedisClusterNode().getMasterNodeInfoSet().contains(entry.getKey())) {
                            Jedis jedis = null;
                            try {
                                jedis = entry.getValue().getResource();
                            } finally {
                                assert jedis != null;
                                jedis.close();
                            }
                        }
                    });
        }
        return null;
    }

    /**
     * 获得当前节点所有String
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, String> getNodeString(Jedis jedis, String pattern) {
        if (pattern == null) pattern = "*";
        jedis.select(0);
        return jedis.keys(pattern)
                .stream()
                .filter(key -> ServerConstant.REDIS_STRING.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, jedis::get));
    }

    /**
     * 根据key查找string类型数据
     *
     * @param keys  Set<String> keys
     * @param jedis Jedis
     * @return Map<String, String>
     */
    private Map<String, String> findStringByKeys(Set<String> keys, Jedis jedis) {
        return keys.stream().collect(toMap(key -> key, jedis::get));
    }

    /**
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(Map<String, String> stringMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            stringMap.forEach(jedis::set);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            stringMap.forEach(jedisCluster::set);
        }
    }

    /**
     * 序列化保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllStringSerialize(Map<String, String> stringMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            stringMap.forEach((key, val) -> jedis.set(key.getBytes(), SerializeUtils.serialize(val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            stringMap.forEach((key, val) -> jedisCluster.set(key.getBytes(), SerializeUtils.serialize(val)));
        }
    }

}
