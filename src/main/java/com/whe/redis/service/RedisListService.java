package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Created by trustme on 2017/2/12.
 * RedisListService
 */
@Service
public class RedisListService {

    /**
     * 获得所有list数据
     *
     * @return Map<String, List<String>>
     */
    public Map<String, List<String>> getAllList() {
        final Map<String, List<String>> allList = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            Map<String, List<String>> listMap = getNodeList(jedis);
            jedis.close();
            return listMap;
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            //检查集群节点是否发生变化
            RedisClusterUtils.checkClusterChange(jedisCluster);
            jedisCluster.getClusterNodes().forEach((key, pool) -> {
                if (JedisFactory.getRedisClusterNode().getMasterNodeInfoSet().contains(key)) {
                    Jedis jedis = null;
                    try {
                        jedis = pool.getResource();
                        allList.putAll(getNodeList(jedis));
                    } finally {
                        assert jedis != null;
                        jedis.close();
                    }
                }
            });
        }
        return allList;
    }

    /**
     * 根据key分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    public Page<List<String>> findListPageByKey(int db, String key, int pageNo) {
        Page<List<String>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            jedis.select(db);
            List<String> list = jedis.lrange(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
            //总数据
            page.setTotalRecord(jedis.llen(key));
            page.setPageNo(pageNo);
            page.setResults(list);
            jedis.close();
            return page;
        }
        return null;
    }

    /**
     * 根据索引更新value
     *
     * @param db    db
     * @param index index
     * @param key   key
     * @param value value
     */
    public void lSet(int db, int index, String key, String value) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.lset(key, index, value);
        jedis.close();
    }


    public long lLen(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Long aLong = jedis.llen(key);
        jedis.close();
        return aLong;
    }

    public void lRem(int db, int index, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        String uuid = UUID.randomUUID().toString();
        jedis.lset(key, index, uuid);
        jedis.lrem(key, 0, uuid);
        jedis.close();
    }

    /**
     * 获得当前节点所有list
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, List<String>> getNodeList(Jedis jedis) {
        return jedis.keys("*").stream().filter(key -> ServerConstant.REDIS_LIST.equalsIgnoreCase(jedis.type(key))).collect(toMap(key -> key, key -> jedis.lrange(key, 0, -1)));
    }

    /**
     * 根据key查找list类型数据
     *
     * @param keys  Set<String> keys
     * @param jedis Jedis
     * @return Map<String,List<String>>
     */
    private Map<String, List<String>> findListByKeys(Set<String> keys, Jedis jedis, Page page) {
        return keys.stream().collect(toMap(key -> key, key -> jedis.lrange(key, (page.getPageNo() - 1) * page.getPageSize(), page.getPageNo() * page.getPageSize())));
    }


    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(Map<String, List<String>> listMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key, val)));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key, val)));
        }
    }


    /**
     * 序列化保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllListSerialize(Map<String, List<String>> listMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key.getBytes(), SerializeUtils.serialize(val))));
            jedis.close();
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key.getBytes(), SerializeUtils.serialize(val))));
        }
    }
}
