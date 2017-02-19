package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisSetService
 */
@Service
public class RedisSetService {
    private Set<String> setKeys = new HashSet<>();

    /**
     * 获得所有set数据
     *
     * @return Map<String.Set>
     */
    public Map<String, Set<String>> getAllSet() {
        final Map<String, Set<String>> allSet = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            Map<String, Set<String>> setMap = getNodeSet(jedis);
            jedis.close();
            return setMap;
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
                                allSet.putAll(getNodeSet(jedis));
                            } finally {
                                assert jedis != null;
                                jedis.close();
                            }
                        }
                    });
        }
        return allSet;
    }

    /**
     * 模糊分页查询set类型数据
     *
     * @return Page<Map<String, Set<String>>>
     */
    public Page<Map<String, Set<String>>> findSetPageByQuery(int pageNo, String pattern) {
        Page<Map<String, Set<String>>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            if (pageNo == 1) {
                Set<String> keys = jedis.keys(pattern);
                keys.forEach(key -> {
                    if (ServerConstant.REDIS_SET.equalsIgnoreCase(jedis.type(key))) {
                        setKeys.add(key);
                    }
                });
            }
            //总数据
            page.setTotalRecord(setKeys.size());
            page.setPageNo(pageNo);
            Map<String, Set<String>> setMap = findSetByKeys(setKeys, jedis);
            page.setResults(setMap);
            jedis.close();
            return page;
        }
        return null;
    }

    /**
     * 获得当前节点所有set
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, Set<String>> getNodeSet(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_SET.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, jedis::smembers));
    }

    /**
     * 根据key查找set类型数据
     *
     * @param keys  Set<String> keys
     * @param jedis Jedis
     * @return Map<String,Set<String>>
     */
    private Map<String, Set<String>> findSetByKeys(Set<String> keys, Jedis jedis) {
        return keys.stream().collect(toMap(key -> key, jedis::smembers));
    }

    /**
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(Map<String, List<String>> setMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key, val)));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedisCluster.sadd(key, val)));
        }
    }

    /**
     * 序列化保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSetSerialize(Map<String, List<String>> setMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key.getBytes(), SerializeUtils.serialize(val))));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedisCluster.sadd(key.getBytes(), SerializeUtils.serialize(val))));
        }
    }


}
