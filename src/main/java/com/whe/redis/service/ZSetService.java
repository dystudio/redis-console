package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisZSetService
 */
@Service
public class ZSetService {

    /**
     * 获得所有zSet数据
     *
     * @return Map<String.Set>
     */
    public Map<String, Set<Tuple>> getAllZSet() {
        final Map<String, Set<Tuple>> zSetMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            Map<String, Set<Tuple>> map = getNodeZSet(jedis);
            jedis.close();
            return map;
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
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    public Page<Set<Tuple>> findZSetPageByKey(int db, int pageNo, String key) {
        Page<Set<Tuple>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            jedis.select(db);
            Set<Tuple> tupleSet = jedis.zrangeByScoreWithScores(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
            //总数据
            page.setTotalRecord(jedis.zcard(key));
            page.setPageNo(pageNo);
            page.setResults(tupleSet);
            jedis.close();
            return page;
        }
        return null;
    }

    public void updateZSet(int db, String key, String oldVal, String newVal, double score) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.zrem(key, oldVal);
        jedis.zadd(key, score, newVal);
        jedis.close();
    }

    public void delZSet(int db, String key, String val) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.zrem(key, val);
        jedis.close();
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
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key, score.doubleValue(), elem)));
            jedis.close();
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
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
        }
    }


}
