package com.whe.redis.service;

import com.alibaba.fastjson.JSON;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by wang hongen on 2017/2/24.
 * RedisClusterService
 */
@Service
public class RedisClusterService {

    private ScanParams scanParams = new ScanParams();
    private JedisCluster jedisCluster;

    {
        scanParams.match(ServerConstant.DEFAULT_MATCH);
        jedisCluster = JedisFactory.getJedisCluster();
    }

    public Long hSetNxSerialize(String key, String field, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        return jedisCluster.hsetnx(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(value));
    }

    public Long hSetNx(String key, String field, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        return jedisCluster.hsetnx(key, field, value);
    }

    public Long zAddSerialize(String key, Double score, String member) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        jedisCluster.zadd(key.getBytes(), score, SerializeUtils.serialize(member));
        return 1L;
    }

    public Long zAdd(String key, Double score, String member) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        jedisCluster.zadd(key, score, member);
        return 1L;
    }

    public Long sAddSerialize(String key, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        jedisCluster.sadd(key.getBytes(), SerializeUtils.serialize(value));
        return 1L;
    }

    public Long sAdd(String key, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        jedisCluster.sadd(key, value);
        return 1L;
    }

    public Long lPushSerialize(String key, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        jedisCluster.lpush(key.getBytes(), SerializeUtils.serialize(value));
        return 1L;
    }

    public Long lPush(String key, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists) {
            return 2L;
        }
        jedisCluster.lpush(key, value);
        return 1L;
    }

    public Long setNxSerialize(String key, String value) {
        return jedisCluster.setnx(key.getBytes(), SerializeUtils.serialize(value));
    }

    public Long setNx(String key, String value) {
        return jedisCluster.setnx(key, value);
    }

    /**
     * 序列化保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHashSerialize(Map<String, Map<String, String>> hashMap) {
        hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
    }

    /**
     * 序列化保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSetSerialize(Map<String, Map<String, Number>> zSetMap) {
        zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
    }

    /**
     * 序列化保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSetSerialize(Map<String, List<String>> setMap) {
        setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedisCluster.sadd(key.getBytes(), SerializeUtils.serialize(val))));
    }

    /**
     * 序列化保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllListSerialize(Map<String, List<String>> listMap) {
        listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key.getBytes(), SerializeUtils.serialize(val))));
    }

    /**
     * 序列化保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllStringSerialize(Map<String, String> stringMap) {
        stringMap.forEach(this::setNxSerialize);
    }

    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(Map<String, Map<String, String>> hashMap) {
        hashMap.forEach((key, map) -> map.forEach((field, val) -> jedisCluster.hset(key, field, val)));
    }


    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(Map<String, Map<String, Number>> zSetMap) {
        zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedisCluster.zadd(key, score.doubleValue(), elem)));
    }

    /**
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(Map<String, List<String>> setMap) {
        setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedisCluster.sadd(key, val)));
    }

    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(Map<String, List<String>> listMap) {
        listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key, val)));
    }

    /**
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(Map<String, String> stringMap) {
        stringMap.forEach(jedisCluster::set);
    }

    /**
     * 获得所有数据
     *
     * @return Map<String, String>
     */
    public String getAll() {
        ScanParams scanParams = new ScanParams();
        scanParams.count(1000);
        scanParams.match(ServerConstant.DEFAULT_MATCH);
        List<String> keys = jedisCluster.getClusterNodes().entrySet().stream().filter(entry -> JedisFactory.getRedisClusterNode().getMasterNode().contains(entry.getKey())).map(entry -> {
            Jedis jedis = null;
            try {
                jedis = entry.getValue().getResource();
                String cursor = ServerConstant.DEFAULT_CURSOR;
                List<String> list = new ArrayList<>(jedis.dbSize().intValue());
                do {
                    ScanResult<String> scan = jedis.scan(cursor, scanParams);
                    cursor = scan.getStringCursor();
                    list.addAll(scan.getResult());
                } while (!ServerConstant.DEFAULT_CURSOR.equals(cursor));
                return list;
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }).flatMap(Collection::stream).collect(toList());

        Map<String, String> stringMap = new HashMap<>();
        Map<String, List<String>> listMap = new HashMap<>();
        Map<String, Set<String>> setMap = new HashMap<>();
        Map<String, Set<Tuple>> tupleMap = new HashMap<>();
        Map<String, Map<String, String>> hashMap = new HashMap<>();

        keys.forEach(key -> {
            String type = jedisCluster.type(key);
            if (ServerConstant.REDIS_STRING.equals(type)) {
                stringMap.put(key, jedisCluster.get(key));
            } else if (ServerConstant.REDIS_LIST.equals(type)) {
                listMap.put(key, jedisCluster.lrange(key, 0, -1));
            } else if (ServerConstant.REDIS_SET.equals(type)) {
                setMap.put(key, jedisCluster.smembers(key));
            } else if (ServerConstant.REDIS_ZSET.equals(key)) {
                tupleMap.put(key, jedisCluster.zrangeWithScores(key, 0, -1));
            } else if (ServerConstant.REDIS_HASH.equals(type)) {
                hashMap.put(key, jedisCluster.hgetAll(key));
            }
        });
        Map<String, Object> map = new HashMap<>();
        if (stringMap.size() > 0) {
            map.put(ServerConstant.REDIS_STRING, stringMap);
        }
        if (listMap.size() > 0) {
            map.put(ServerConstant.REDIS_LIST, listMap);
        }
        if (setMap.size() > 0) {
            map.put(ServerConstant.REDIS_SET, setMap);
        }
        if (tupleMap.size() > 0) {
            Map<String, Double> stringDoubleMap = new HashMap<>();
            Map<String, Map<String, Double>> zSetMap = new HashMap<>();
            tupleMap.forEach((key, set) -> {
                set.forEach(t -> stringDoubleMap.put(t.getElement(), t.getScore()));
                zSetMap.put(key, new HashMap<>(stringDoubleMap));
                stringDoubleMap.clear();
            });
            map.put(ServerConstant.REDIS_ZSET, zSetMap);
        }
        if (hashMap.size() > 0) {
            map.put(ServerConstant.REDIS_HASH, hashMap);
        }
        return JSON.toJSONString(map);
    }

    public void hSet(String key, String field, String val) {
        jedisCluster.hset(key, field, val);
    }

    public Boolean updateHash(String key, String oldField, String newField, String val) {
        Boolean hExists = jedisCluster.hexists(key, newField);
        if (hExists) {
            return false;
        }
        jedisCluster.hdel(key, oldField);
        jedisCluster.hset(key, newField, val);
        return true;
    }

    public void delHash(String key, String field) {
        jedisCluster.hdel(key, field);
    }

    public Map<String, String> hGetAll(String key) {
        return jedisCluster.hgetAll(key);
    }

    public void delZSet(String key, String val) {
        jedisCluster.zrem(key, val);
    }

    public void updateZSet(String key, String oldVal, String newVal, double score) {
        jedisCluster.zrem(key, oldVal);
        jedisCluster.zadd(key, score, newVal);
    }

    /**
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    public Page<Set<Tuple>> findZSetPageByKey(int pageNo, String key) {
        Page<Set<Tuple>> page = new Page<>();
        Set<Tuple> tupleSet = jedisCluster.zrangeByScoreWithScores(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
        //总数据
        page.setTotalRecord(jedisCluster.zcard(key));
        page.setPageNo(pageNo);
        page.setResults(tupleSet);
        return page;
    }

    public void updateSet(String key, String oldVal, String newVal) {
        jedisCluster.srem(key, oldVal);
        jedisCluster.sadd(key, newVal);
    }

    public void delSet(String key, String val) {
        jedisCluster.srem(key, val);
    }

    /**
     * 查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(String key) {
        return jedisCluster.smembers(key);
    }

    public void lRem(int index, String key) {
        String uuid = UUID.randomUUID().toString();
        jedisCluster.lset(key, index, uuid);
        jedisCluster.lrem(key, 0, uuid);
    }

    public Long lLen(String key) {
        return jedisCluster.llen(key);
    }

    /**
     * 根据索引更新value
     *
     * @param index index
     * @param key   key
     * @param value value
     */
    public void lSet(int index, String key, String value) {
        jedisCluster.lset(key, index, value);
    }


    /**
     * 根据key分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    public Page<List<String>> findListPageByKey(String key, int pageNo) {
        Page<List<String>> page = new Page<>();
        List<String> list = jedisCluster.lrange(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
        //总数据
        page.setTotalRecord(jedisCluster.llen(key));
        page.setPageNo(pageNo);
        page.setResults(list);
        return page;
    }

    public void set(String key, String val) {
        jedisCluster.set(key, val);
    }

    public String get(String key) {
        return jedisCluster.get(key);
    }

    public Long del(String key) {
        return jedisCluster.del(key);
    }

    public Long renameNx(String oldKey, String newKey) {
        return jedisCluster.renamenx(oldKey, newKey);
    }

    public Long expire(String key, int seconds) {
        return jedisCluster.expire(key, seconds);
    }

    public void persist(String key) {
        jedisCluster.persist(key);
    }

    public long ttl(String key) {
        return jedisCluster.ttl(key);
    }

    public Map<String, ScanResult<String>> scan(Map<String, String> nodeCursor, String match) {
        scanParams.match(match);
        Map<String, ScanResult<String>> nodeScan = new HashMap<>(ServerConstant.PAGE_NUM * JedisFactory.getRedisClusterNode().getMasterNode().size());
        int count = ServerConstant.PAGE_NUM / JedisFactory.getRedisClusterNode().getMasterNode().size();
        jedisCluster.getClusterNodes().entrySet().stream().filter(entry -> JedisFactory.getRedisClusterNode().getMasterNode().contains(entry.getKey())).forEach(entry -> {
            Jedis jedis = null;
            try {
                jedis = entry.getValue().getResource();
                String cursor = nodeCursor.get(entry.getKey());
                if (cursor == null) {
                    nodeCursor.put(entry.getKey(), ServerConstant.DEFAULT_CURSOR);
                    cursor = nodeCursor.get(entry.getKey());
                }
                ScanResult<String> scan = jedis.scan(cursor, scanParams);
                if (scan.getResult().size() < count) {
                    scanParams.count(count - scan.getResult().size() + count);
                } else {
                    scanParams.count(count);
                }
                nodeScan.put(entry.getKey(), jedis.scan(cursor, scanParams));
            } catch (JedisConnectionException e) {
                Set<String> slaveNode = JedisFactory.getRedisClusterNode().getSlaveNode();
                for (String node : slaveNode) {
                    String[] split = node.split(":");
                    Jedis j = null;
                    try {
                        j = new Jedis(split[0], Integer.parseInt(split[1]));
                        JedisFactory.setRedisClusterNode(new com.whe.redis.util.RedisClusterNode(node, j.clusterNodes()));
                        break;
                    } catch (Exception ignored) {
                        // try next nodes
                    } finally {
                        if (j != null) {
                            j.close();
                        }
                    }
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        });
        return nodeScan;
    }

    public Map<String, String> getType(List<String> keys) {
        return keys.stream().collect(toMap(key -> key, jedisCluster::type));
    }

    /**
     * 删除所有数据
     */
    public void flushAll() {
        jedisCluster.getClusterNodes().forEach((key, pool) -> {
            if (JedisFactory.getRedisClusterNode().getMasterNode().contains(key)) {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.flushAll();
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
        });
    }
}
