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
    private Set<String> listKeys = new HashSet<>();

    /**
     * 获得所有list数据
     *
     * @return Map<String, List<String>>
     */
    public Map<String, List<String>> getAllList() {
        final Map<String, List<String>> allList = new HashMap<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            return getNodeList(JedisFactory.getJedis());
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            //检查集群节点是否发生变化
            RedisClusterUtils.checkClusterChange(jedisCluster);
            jedisCluster.getClusterNodes()
                    .forEach((key, pool) -> {
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
     * 模糊分页查询list类型数据
     *
     * @return Page<Map<String, List<String>>>
     */
    public Page<Map<String, List<String>>> findListPageByQuery(int pageNo, String pattern) {
        Page<Map<String, List<String>>> page = new Page<>();
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            if (pageNo == 1) {
                Set<String> keys = jedis.keys(pattern);
                keys.forEach(key -> {
                    if (ServerConstant.REDIS_LIST.equalsIgnoreCase(jedis.type(key))) {
                        listKeys.add(key);
                    }
                });
            }
            //总数据
            page.setTotalRecord(listKeys.size());
            page.setPageNo(pageNo);
            Map<String, List<String>> listMap = findListByKeys(listKeys, jedis, page);
            page.setResults(listMap);
            return page;
        }
        return null;
    }

    /**
     * 获得当前节点所有list
     *
     * @param jedis jedis
     * @return Map
     */
    private Map<String, List<String>> getNodeList(Jedis jedis) {
        return jedis.keys("*")
                .stream()
                .filter(key -> ServerConstant.REDIS_LIST.equalsIgnoreCase(jedis.type(key)))
                .collect(toMap(key -> key, key -> jedis.lrange(key, 0, -1)));
    }

    /**
     * 根据key查找list类型数据
     *
     * @param keys  Set<String> keys
     * @param jedis Jedis
     * @return Map<String, List<String>>
     */
    private Map<String, List<String>> findListByKeys(Set<String> keys, Jedis jedis, Page page) {
        return keys.stream().collect(toMap(key -> key, key ->
                jedis.lrange(key, (page.getPageNo() - 1) * page.getPageSize(), page.getPageNo() * page.getPageSize())
        ));
    }


    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(Map<String, List<String>> listMap) {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedis();
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key, val)));
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
            Jedis jedis = JedisFactory.getJedis();
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key.getBytes(), SerializeUtils.serialize(val))));
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisCluster jedisCluster = JedisFactory.getJedisCluster();
            listMap.forEach((key, list) -> list.forEach(val -> jedisCluster.lpush(key.getBytes(), SerializeUtils.serialize(val))));
        }
    }
}
