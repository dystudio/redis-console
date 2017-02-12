package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisZSetService
 */
@Service
public class RedisZSetService {
    private Set<String> zSetKeys = new HashSet<>();

    /**
     * 获得所有zSet数据
     *
     * @return Map<String.Set>
     */
    public Map<String, Set<Tuple>> getAllZSet() {
        final Map<String, Set<Tuple>> zSetMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeZSet(JedisFactory.getJedis());
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
                                zSetMap.putAll(getNodeZSet(jedis));
                            } finally {
                                assert jedis != null;
                                jedis.close();
                            }
                        }
                    });
        }
        return zSetMap;
    }

    /**
     * 模糊分页查询zSet类型数据
     *
     * @return Page<Map<String, Set<Tuple>>>
     */
    public Page<Map<String, Set<Tuple>>> findZSetPageByQuery(int pageNo, String pattern) {
        Page<Map<String, Set<Tuple>>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            if (pageNo == 1) {
                Set<String> keys = jedis.keys(pattern);
                keys.forEach(key -> {
                    if (ServerConstant.REDIS_ZSET.equalsIgnoreCase(jedis.type(key))) {
                        zSetKeys.add(key);
                    }
                });
            }
            //总数据
            page.setTotalRecord(zSetKeys.size());
            page.setPageNo(pageNo);
            Map<String, Set<Tuple>> zSetMap = findZSetByKeys(zSetKeys, jedis);
            page.setResults(zSetMap);
            return page;
        }
        return null;
    }

    /**
     * 获得当前节点所有zSet
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, Set<Tuple>> getNodeZSet(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_ZSET.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, key -> jedis.zrangeWithScores(key, 0, -1)));
    }

    /**
     * 根据key查找zSet类型数据
     *
     * @param keys  Set<String> keys
     * @param jedis Jedis
     * @return Map<String, Set<Tuple>>
     */
    private Map<String, Set<Tuple>> findZSetByKeys(Set<String> keys, Jedis jedis) {
        return keys.stream().collect(toMap(key -> key, key -> jedis.zrangeWithScores(key, 0, -1)));
    }

    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(Map<String, Map<String, Number>> zSetMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key, score.doubleValue(), elem)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key, score.doubleValue(), elem)));
        }
    }

    /**
     * 序列化保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSetSerialize(Map<String, Map<String, Number>> zSetMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
        }
    }


}
