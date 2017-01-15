package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
@Service
public class JedisService {
    /**
     * 获得所有string类型数据
     *
     * @return Map<String.String>
     */
    public Map<String, String> getAllString() {
        final Map<String, String> allString = new HashMap<>();
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeString(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedisCluster().getClusterNodes()
                    .forEach((key, pool) -> {
                        Jedis jedis = null;
                        try {
                            jedis = pool.getResource();
                            allString.putAll(getNodeString(jedis));
                        } finally {
                            assert jedis != null;
                            jedis.close();
                        }
                    });
        }
        return allString;
    }

    /**
     * 获得所有list数据
     *
     * @return Map<String.List>
     */
    public Map<String, List<String>> getAllList() {
        final Map<String, List<String>> allList = new HashMap<>();
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeList(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedisCluster().getClusterNodes()
                    .forEach((key, pool) -> {
                        Jedis jedis = null;
                        try {
                            jedis = pool.getResource();
                            allList.putAll(getNodeList(jedis));
                        } finally {
                            assert jedis != null;
                            jedis.close();
                        }
                    });
        }
        return allList;
    }

    /**
     * 获得所有set数据
     *
     * @return Map<String.Set>
     */
    public Map<String, Set<String>> getAllSet() {
        final Map<String, Set<String>> allSet = new HashMap<>();
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeSet(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedisCluster().getClusterNodes()
                    .forEach((key, pool) -> {
                        Jedis jedis = null;
                        try {
                            jedis = pool.getResource();
                            allSet.putAll(getNodeSet(jedis));
                        } finally {
                            assert jedis != null;
                            jedis.close();
                        }
                    });
        }
        return allSet;
    }

    /**
     * 获得所有zSet数据
     *
     * @return Map<String.Set>
     */
    public Map<String, Set<Tuple>> getAllZSet() {
        final Map<String, Set<Tuple>> zSetMap = new HashMap<>();
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeZSet(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedisCluster().getClusterNodes()
                    .forEach((key, pool) -> {
                        Jedis jedis = null;
                        try {
                            jedis = pool.getResource();
                            zSetMap.putAll(getNodeZSet(jedis));
                        } finally {
                            assert jedis != null;
                            jedis.close();
                        }
                    });
        }
        return zSetMap;
    }

    /**
     * 获得所有hash数据
     *
     * @return map
     */
    public Map<String, Map<String, String>> getAllHash() {
        final Map<String, Map<String, String>> hashMap = new HashMap<>();
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeHash(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedisCluster().getClusterNodes()
                    .forEach((key, pool) -> {
                        Jedis jedis = null;
                        try {
                            jedis = pool.getResource();
                            hashMap.putAll(getNodeHash(jedis));
                        } finally {
                            assert jedis != null;
                            jedis.close();
                        }
                    });
        }
        return hashMap;
    }

    /**
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(Map<String, String> stringMap) {
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            stringMap.forEach(jedis::set);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            stringMap.forEach(jedisCluster::set);
        }
    }

    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(Map<String, List<String>> listMap) {
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key, val)));
        }
    }

    /**
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(Map<String, List<String>> setMap) {
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedisCluster.sadd(key, val)));
        }
    }

    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(Map<String, Map<String, Number>> zSetMap) {
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key, score.doubleValue(), elem)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key, score.doubleValue(), elem)));
        }
    }

    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(Map<String, Map<String, String>> hashMap) {
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key, field, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key, field, val)));
        }
    }

    /**
     * 删除所有数据
     */
    public void flushAll() {
        if (ServerConstant.SINGLE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedis().flushAll();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedisCluster()
                    .getClusterNodes()
                    .forEach((key, pool) -> {
                        Jedis jedis = null;
                        try {
                            jedis = pool.getResource();
                            jedis.flushAll();
                        } finally {
                            assert jedis != null;
                            jedis.close();
                        }
                    });
        }
    }

    /**
     * 获得当前节点所有String
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, String> getNodeString(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_STRING.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, jedis::get));
    }

    /**
     * 获得当前节点所有list
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, List<String>> getNodeList(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_LIST.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, key -> jedis.lrange(key, 0, -1)));
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


}
