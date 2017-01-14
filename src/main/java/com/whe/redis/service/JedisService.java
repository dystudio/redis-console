package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.ConcurrentSkipListMap.toList;
import static java.util.stream.Collectors.reducing;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
@Service
public class JedisService {

    public Map<String, String> getAllString() {
        Map<String, String> allString = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            allString = getNodeString(jedis);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                JedisPool jp = clusterNodes.get(key);
                try (Jedis jedis = jp.getResource()) {
                    allString.putAll(getNodeString(jedis));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return allString;
    }

    public Map<String, List<String>> getAllList() {
        Map<String, List<String>> allList = new HashMap<>();

        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            allList = getNodeList(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                JedisPool jp = clusterNodes.get(key);
                try (Jedis jedis = jp.getResource()) {
                    allList.putAll(getNodeList(jedis));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return allList;
    }

    public Map<String, Set<String>> getAllSet() {
        Map<String, Set<String>> allSet = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            allSet = getNodeSet(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                JedisPool jp = clusterNodes.get(key);
                Jedis jedis = jp.getResource();
                jedis.flushAll();
                try {
                    allSet.putAll(getNodeSet(jedis));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    jedis.close();//用完一定要close这个链接！！！
                }
            }
        }
        return allSet;
    }


    public Map<String, Set<String>> getAllZSet() {
        Map<String, Set<String>> zSetMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            zSetMap = getNodeZSet(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                JedisPool jp = clusterNodes.get(key);
                try (Jedis jedis = jp.getResource()) {
                    zSetMap.putAll(getNodeZSet(jedis));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return zSetMap;
    }

    public Map<String, Map<String, String>> getAllHash() {
        Map<String, Map<String, String>> hashMap = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            hashMap = getNodeHash(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                JedisPool jp = clusterNodes.get(key);
                try (Jedis jedis = jp.getResource()) {
                    hashMap.putAll(getNodeHash(jedis));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return hashMap;
    }

    public void saveAllString(Map<String, String> stringMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            for (Map.Entry entry : stringMap.entrySet()) {
                jedis.set(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            for (Map.Entry entry : stringMap.entrySet()) {
                jedisCluster.set(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }

    public void saveAllList(Map<String, List<String>> listMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            for (Map.Entry<String, List<String>> entry : listMap.entrySet()) {
                for (String val : entry.getValue()) {
                    jedis.lpush(String.valueOf(entry.getKey()), String.valueOf(val));
                }
            }
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            for (Map.Entry<String, List<String>> entry : listMap.entrySet()) {
                for (String val : entry.getValue()) {
                    jedisCluster.lpush(String.valueOf(entry.getKey()), String.valueOf(val));
                }
            }
        }
    }

    public void saveAllSet(Map<String, Set<String>> setMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            for (Map.Entry<String, Set<String>> entry : setMap.entrySet()) {
                for (String val : entry.getValue()) {
                    jedis.sadd(String.valueOf(entry.getKey()), String.valueOf(val));
                }
            }
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            for (Map.Entry<String, Set<String>> entry : setMap.entrySet()) {
                for (String val : entry.getValue()) {
                    jedisCluster.sadd(String.valueOf(entry.getKey()), String.valueOf(val));
                }
            }
        }
    }

    public void saveAllHash(Map<String, Map<String, String>> hashMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            for (Map.Entry<String, Map<String, String>> entry : hashMap.entrySet()) {
                for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                    jedis.hset(String.valueOf(entry.getKey()), String.valueOf(entry1.getKey()), String.valueOf(entry1.getValue()));
                }
            }
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            for (Map.Entry<String, Map<String, String>> entry : hashMap.entrySet()) {
                for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                    jedisCluster.hset(String.valueOf(entry.getKey()), String.valueOf(entry1.getKey()), String.valueOf(entry1.getValue()));
                }
            }
        }
    }


    public void flushAll() {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            String s = jedis.flushAll();
            System.out.println(s);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                JedisPool jp = clusterNodes.get(key);
                try (Jedis jedis = jp.getResource()) {
                    jedis.flushAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获得当前节点所有String
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, String> getNodeString(Jedis jedis) {
        Map<String, String> strMap = new HashMap<>();
        Set<String> keys = jedis.keys("*");
        Stream<String> stringStream = keys.stream()
                .filter(key -> ServerConstant.REDIS_STRING.equalsIgnoreCase(jedis.type(key)));
        stringStream.map(jedis::get);

        for (String key : keys) {
            if (ServerConstant.REDIS_STRING.equalsIgnoreCase(jedis.type(key))) {
                String str = jedis.get(key);
                strMap.put(key, str);
            }
        }
        return strMap;
    }

    private Map<String, List<String>> getNodeList(Jedis jedis) {
        Map<String, List<String>> listMap = new HashMap<>();
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            if (ServerConstant.REDIS_LIST.equalsIgnoreCase(jedis.type(key))) {
                List<String> values = jedis.lrange(key, 0, -1);
                listMap.put(key, values);
            }
        }
        return listMap;
    }

    private Map<String, Set<String>> getNodeSet(Jedis jedis) {
        Map<String, Set<String>> setMap = new HashMap<>();
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            if (ServerConstant.REDIS_SET.equalsIgnoreCase(jedis.type(key))) {
                Set<String> setValues = jedis.smembers(key);
                setMap.put(key, setValues);
            }
        }
        return setMap;
    }


    private Map<String, Set<String>> getNodeZSet(Jedis jedis) {
        Map<String, Set<String>> zSetMap = new HashMap<>();
        Set<String> keys = jedis.keys("*");
        keys.stream().filter(key -> ServerConstant.REDIS_ZSET.equalsIgnoreCase(jedis.type(key)))
                .map(key -> jedis.zrange(key, 0, -1)).forEach(System.out::println);
        for (String key : keys) {
            if (ServerConstant.REDIS_ZSET.equalsIgnoreCase(jedis.type(key))) {
                Set<String> setValues = jedis.zrange(key, 0, -1);
                zSetMap.put(key, setValues);
            }
        }
        return zSetMap;
    }

    private Map<String, Map<String, String>> getNodeHash(Jedis jedis) {
        Map<String, Map<String, String>> hashMap = new HashMap<>();
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            if (ServerConstant.REDIS_HASH.equalsIgnoreCase(jedis.type(key))) {
                Map<String, String> map = jedis.hgetAll(key);
                hashMap.put(key, map);
            }
        }
        return hashMap;
    }
}
