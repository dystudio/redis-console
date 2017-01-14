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

    public Map<String, String> getAllString() {
        final Map<String, String> allString = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
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

    public Map<String, List<String>> getAllList() {
        final Map<String, List<String>> allList = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
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

    public Map<String, Set<String>> getAllSet() {
        final Map<String, Set<String>> allSet = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
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


    public Map<String, Set<Tuple>> getAllZSet() {
        final Map<String, Set<Tuple>> zSetMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
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

    public Map<String, Map<String, String>> getAllHash() {
        final Map<String, Map<String, String>> hashMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
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

    public void saveAllString(Map<String, String> stringMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            stringMap.forEach(jedis::set);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            stringMap.forEach(jedisCluster::set);
        }
    }

    public void saveAllList(Map<String, List<String>> listMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key, val)));
        }
    }

    public void saveAllSet(Map<String, List<String>> setMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedisCluster.sadd(key, val)));
        }
    }

    public void saveAllZSet(Map<String, Map<String, Number>> zSetMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key, score.doubleValue(), elem)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key, score.doubleValue(), elem)));
        }
    }

    public void saveAllHash(Map<String, Map<String, String>> hashMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key, field, val)));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key, field, val)));
        }
    }

    public void flushAll() {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
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

    private Map<String, List<String>> getNodeList(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_LIST.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, key -> jedis.lrange(key, 0, -1)));
    }

    private Map<String, Set<String>> getNodeSet(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_SET.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, jedis::smembers));
    }


    private Map<String, Set<Tuple>> getNodeZSet(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_ZSET.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, key -> jedis.zrangeWithScores(key, 0, -1)));
    }

    private Map<String, Map<String, String>> getNodeHash(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_HASH.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, jedis::hgetAll));
    }


}
