package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.RedisClusterUtils;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
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
public class SetService {

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
            jedisCluster.getClusterNodes().forEach((key, pool) -> {
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
     * 查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(int db, String key) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            jedis.select(db);
            Set<String> set = jedis.smembers(key);
            jedis.close();
            return set;
        }
        return null;
    }

    public void updateSet(int db, String key, String oldVal, String newVal) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.srem(key, oldVal);
        jedis.sadd(key, newVal);
        jedis.close();
    }

    public void delSet(int db, String key, String val) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.srem(key, val);
        jedis.close();
    }

    /**
     * 获得当前节点所有set
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, Set<String>> getNodeSet(Jedis jedis) {
        return jedis.keys("*").stream().filter(key -> ServerConstant.REDIS_SET.equalsIgnoreCase(jedis.type(key))).collect(toMap(key -> key, jedis::smembers));
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
