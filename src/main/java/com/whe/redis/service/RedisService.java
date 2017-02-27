package com.whe.redis.service;

import com.alibaba.fastjson.JSON;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
@Service
public class RedisService {
    public String backup() {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        int size = Integer.parseInt(jedis.configGet(ServerConstant.DATABASES).get(1));
        Map<String, Object> dbKeys = new HashMap<>();
        for (int i = 0; i < size; i++) {
            jedis.select(i);
            String cursor = ServerConstant.DEFAULT_CURSOR;
            ScanParams scanParams = new ScanParams();
            scanParams.count(1000);
            scanParams.match(ServerConstant.DEFAULT_MATCH);
            List<String> keys = new ArrayList<>(jedis.dbSize().intValue());
            do {
                ScanResult<String> scan = jedis.scan(cursor, scanParams);
                cursor = scan.getStringCursor();
                keys.addAll(scan.getResult());
            } while (!ServerConstant.DEFAULT_CURSOR.equals(cursor));

            Map<String, String> stringMap = new HashMap<>();
            Map<String, List<String>> listMap = new HashMap<>();
            Map<String, Set<String>> setMap = new HashMap<>();
            Map<String, Set<Tuple>> tupleMap = new HashMap<>();
            Map<String, Map<String, String>> hashMap = new HashMap<>();
            keys.forEach(key -> {
                String type = jedis.type(key);
                if (ServerConstant.REDIS_STRING.equals(type)) {
                    stringMap.put(key, jedis.get(key));
                } else if (ServerConstant.REDIS_HASH.equals(type)) {
                    hashMap.put(key, jedis.hgetAll(key));
                } else if (ServerConstant.REDIS_LIST.equals(type)) {
                    listMap.put(key, jedis.lrange(key, 0, -1));
                } else if (ServerConstant.REDIS_SET.equals(type)) {
                    setMap.put(key, jedis.smembers(key));
                } else if (ServerConstant.REDIS_ZSET.equalsIgnoreCase(key)) {
                    tupleMap.put(key, jedis.zrangeWithScores(key, 0, -1));
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
                Map<String, Map<String, Double>> zSetMap = tupleMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().collect(Collectors.toMap(Tuple::getElement, Tuple::getScore))));
                map.put(ServerConstant.REDIS_ZSET, zSetMap);
            }
            if (hashMap.size() > 0) {
                map.put(ServerConstant.REDIS_HASH, hashMap);
            }
            if (map.size() > 0) {
                dbKeys.put(i + "", map);
            }
        }
        return JSON.toJSONString(dbKeys);
    }

    public ScanResult<String> getKeysByDb(int db, String cursor) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        ScanParams scanParams = new ScanParams();
        scanParams.count(ServerConstant.PAGE_NUM);
        scanParams.match(ServerConstant.DEFAULT_MATCH);
        if (cursor == null) {
            cursor = ServerConstant.DEFAULT_CURSOR;
        }
        ScanResult<String> scan = jedis.scan(cursor, scanParams);
        jedis.close();
        return scan;
    }

    /**
     * 更新key
     *
     * @param db     db
     * @param oldKey 旧key
     * @param newKey 新key
     */
    public long renameNx(int db, String oldKey, String newKey) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Long aLong = jedis.renamenx(oldKey, newKey);
        jedis.close();
        return aLong;
    }

    /**
     * @param db  db
     * @param key key
     * @return long
     */
    public long ttl(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Long ttl = jedis.ttl(key);
        jedis.close();
        return ttl;
    }

    /**
     * 更新生存时间
     *
     * @param db      db
     * @param key     key
     * @param seconds 秒
     */
    public void setExpire(int db, String key, int seconds) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.expire(key, seconds);
        jedis.close();
    }

    /**
     * 删除key
     *
     * @param db  db
     * @param key key
     */
    public void delKey(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.del(key);
        jedis.close();
    }


    public Map<String, String> getType(int db, List<String> keys) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Map<String, String> map = keys.stream().collect(Collectors.toMap(key -> key, jedis::type));
        jedis.close();
        return map;
    }

    public Map<Integer, Long> getDataBases() {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        List<String> list = jedis.configGet(ServerConstant.DATABASES);
        int size = Integer.parseInt(list.get(1));
        Map<Integer, Long> map = IntStream.range(0, size).boxed().collect(Collectors.toMap(i -> i, i -> {
            jedis.select(i);
            return jedis.dbSize();
        }));
        jedis.close();
        return map;
    }


    /**
     * 删除所有数据
     */
    public void flushAll() {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.flushAll();
        jedis.close();
    }

}
