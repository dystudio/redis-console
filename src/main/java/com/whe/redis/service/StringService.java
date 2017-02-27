package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.SerializeUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by trustme on 2017/2/12.
 * RedisStringService
 */
@Service
public class StringService {


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
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(int db,Map<String, String> stringMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        stringMap.forEach(jedis::set);
        jedis.close();
    }

    /**
     * 序列化保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllStringSerialize(int db,Map<String, String> stringMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        stringMap.forEach((key, val) -> jedis.set(key.getBytes(), SerializeUtils.serialize(val)));
        jedis.close();
    }

}