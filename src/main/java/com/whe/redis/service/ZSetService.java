package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Map;
import java.util.Set;

/**
 * Created by trustme on 2017/2/12.
 * RedisZSetService
 */
@Service
public class ZSetService {
    public Long saveSerialize(int db, String key, double score, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists) {
            return 2L;
        }
        jedis.zadd(key.getBytes(), score, SerializeUtils.serialize(value));
        jedis.close();
        return 1L;
    }

    public Long save(int db, String key, double score, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists) {
            return 2L;
        }
        jedis.zadd(key, score, value);
        jedis.close();
        return 1L;
    }

    /**
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    public Page<Set<Tuple>> findZSetPageByKey(int db, int pageNo, String key) {
        Page<Set<Tuple>> page = new Page<>();
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Set<Tuple> tupleSet = jedis.zrangeByScoreWithScores(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
        //总数据
        page.setTotalRecord(jedis.zcard(key));
        page.setPageNo(pageNo);
        page.setResults(tupleSet);
        jedis.close();
        return page;
    }

    public void updateZSet(int db, String key, String oldVal, String newVal, double score) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.zrem(key, oldVal);
        jedis.zadd(key, score, newVal);
        jedis.close();
    }

    public void delZSet(int db, String key, String val) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.zrem(key, val);
        jedis.close();
    }


    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(int db, Map<String, Map<String, Number>> zSetMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key, score.doubleValue(), elem)));
        jedis.close();
    }

    /**
     * 序列化保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSetSerialize(int db, Map<String, Map<String, Number>> zSetMap) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
        jedis.close();
    }


}
