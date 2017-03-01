package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.SerializeUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by trustme on 2017/2/12.
 * RedisSetService
 */
@Service
public class SetService {

    public Long save(int db, String key, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists) {
            return 2L;
        }
        jedis.sadd(key, value);
        jedis.close();
        return 1L;
    }

    /**
     * 查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Set<String> set = jedis.smembers(key);
        jedis.close();
        return set;
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
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(int db, Map<String, List<String>> setMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key, val)));
        jedis.close();
    }

    /**
     * 序列化保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSetSerialize(int db, Map<String, List<String>> setMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key.getBytes(), SerializeUtils.serialize(val))));
        jedis.close();
    }


}
