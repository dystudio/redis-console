package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by wang hongen on 2017/2/12.
 * RedisZSetService
 */
@Service
public class ZSetService {

    public Long zAdd(int db, String key, double score, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !jedis.type(key).equals(ServerConstant.REDIS_ZSET)) {
            return 2L;
        }
        jedis.zadd(key, score, value);
        jedis.close();
        return 1L;
    }

    public Long zAddSerialize(int db, String key, double score, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !jedis.type(key).equals(ServerConstant.REDIS_ZSET)) {
            return 2L;
        }
        jedis.zadd(key.getBytes(), score, SerializeUtils.serialize(value));
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

    public Page<Set<Tuple>> findZSetPageByKeySerialize(int db, String key, int pageNo) {
        Page<Set<Tuple>> page = new Page<>();
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Set<Tuple> tupleSet = null;
        try {
            tupleSet = jedis.zrangeByScoreWithScores(key.getBytes(ServerConstant.CHARSET), (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM)
                    .stream()
                    .map(tuple -> new Tuple(SerializeUtils.unSerialize(tuple.getBinaryElement()).toString(), tuple.getScore()))
                    .collect(Collectors.toSet());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //总数据
        page.setTotalRecord(jedis.llen(key));
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
    public void saveAllZSet(int db, Map<String, Map<String, Double>> zSetMap) {
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
