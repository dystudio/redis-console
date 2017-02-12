package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.RedisClusterUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
@Service
public class RedisService {

    public Set<String> keys() {
        Jedis jedis = JedisFactory.getJedis();
        Long db = jedis.getDB();
        jedis.select(0);
        System.out.println("sdfsdf");
        return jedis.keys("*");
    }

    public Map<Integer, Long> getDataBases() {
        Jedis jedis = JedisFactory.getJedis();
        List<String> list = jedis.configGet(ServerConstant.DATABASES);
        int size = Integer.parseInt(list.get(1));
        Map<Integer, Long> dbSize = new HashMap<>();
        for (int i = 0; i < size; i++) {
            jedis.select(i);
            dbSize.put(i, jedis.dbSize());
        }
        return dbSize;
    }


    /**
     * 删除所有数据
     */
    public void flushAll() {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            JedisFactory.getJedis().flushAll();
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
                                jedis.flushAll();
                            } finally {
                                assert jedis != null;
                                jedis.close();
                            }
                        }
                    });
        }
    }


}
