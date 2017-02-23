package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.RedisClusterUtils;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisHashService
 */
@Service
public class RedisHashService {

    /**
     * 获得所有hash数据
     *
     * @return map
     */
    public Map<String, Map<String, String>> getAllHash() {
        final Map<String, Map<String, String>> hashMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            Map<String, Map<String, String>> nodeHash = getNodeHash(jedis);
            jedis.close();
            return nodeHash;
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

    public Map<String, String> hGetAll(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Map<String, String> map = jedis.hgetAll(key);
        jedis.close();
        return map;
    }

    public void hSet(int db,String key,String field,String val){
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.hset(key,field,val);
        jedis.close();
    }
    public void updateHash(int db,String key,String oldField,String newField,String val){
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.hdel(key,oldField);
        jedis.hset(key,newField,val);
        jedis.close();
    }
    public void delHash(int db,String key,String field){
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.hdel(key,field);
        jedis.close();
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
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
            jedis.close();
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
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key, field, val)));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key, field, val)));
        }
    }


}
