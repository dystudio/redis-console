package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by trustme on 2017/2/12.
 * RedisSetService
 */
@Service
public class SetService {


    public Long sAddSerialize(int db, String key, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_SET.equals(jedis.type(key))) {
            return 2L;
        }
        jedis.sadd(key.getBytes(), SerializeUtils.serialize(value));
        jedis.close();
        return 1L;
    }

    public Long sAdd(int db, String key, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_SET.equals(jedis.type(key))) {
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

    public Set<String> getSetSerialize(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Set<String> set = null;
        try {
            set = jedis.smembers(key.getBytes(ServerConstant.CHARSET)).stream().map(bytes -> SerializeUtils.unSerialize(bytes).toString()).collect(Collectors.toSet());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
