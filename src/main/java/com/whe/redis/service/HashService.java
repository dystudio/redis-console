package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.SerializeUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by trustme on 2017/2/12.
 * RedisHashService
 */
@Service
public class HashService {
    public Long save(int db, String key, String field, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists) {
            return 2L;
        }
        Long l = jedis.hsetnx(key, field, value);
        jedis.close();
        return l;
    }

    public Map<String, String> hGetAll(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Map<String, String> map = jedis.hgetAll(key);
        jedis.close();
        return map;
    }

    public void hSet(int db, String key, String field, String val) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.hset(key, field, val);
        jedis.close();
    }

    public boolean updateHash(int db, String key, String oldField, String newField, String val) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean hExists = jedis.hexists(key, newField);
        if (hExists) {
            return false;
        }
        jedis.hdel(key, oldField);
        jedis.hset(key, newField, val);
        jedis.close();
        return true;
    }

    public void delHash(int db, String key, String field) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.hdel(key, field);
        jedis.close();
    }

    /**
     * 序列化保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHashSerialize(int db, Map<String, Map<String, String>> hashMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
        jedis.close();
    }


    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(int db, Map<String, Map<String, String>> hashMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key, field, val)));
        jedis.close();
    }


}
