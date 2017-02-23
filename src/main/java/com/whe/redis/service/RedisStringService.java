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
 * RedisStringService
 */
@Service
public class RedisStringService {

    /**
     * 获得所有string类型数据
     *
     * @return Map<String, String>
     */
    public Map<String, String> getAllString(String pattern) {
        final Map<String, String> allString = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            Map<String, String> stringMap = getNodeString(jedis, pattern);
            jedis.close();
            return stringMap;
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
     * 根据数据库和key获得val
     *
     * @param db  db
     * @param key key
     * @return String
     */
    public String getString(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        String val = jedis.get(key);
        jedis.close();
        return val;
    }

    /**
     * 更新val
     *
     * @param db  db
     * @param key key
     * @param val newValue
     */
    public void updateVal(int db, String key, String val) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.set(key, val);
        jedis.close();
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
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            stringMap.forEach(jedis::set);
            jedis.close();
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
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            stringMap.forEach((key, val) -> jedis.set(key.getBytes(), SerializeUtils.serialize(val)));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            stringMap.forEach((key, val) -> jedisCluster.set(key.getBytes(), SerializeUtils.serialize(val)));
        }
    }

}
