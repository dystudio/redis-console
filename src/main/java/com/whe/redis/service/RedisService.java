package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.RedisClusterUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
@Service
public class RedisService {
    private Map<Integer,List<String>> dbKeysMap=new HashMap<>();
    public List<String> getKeysByDb(int db) {
        JedisPool jedisPool=new JedisPool("192.168.88.128",6379);
        Jedis jedis = jedisPool.getResource();
        jedis.select(db);
        ScanParams scanParams = new ScanParams();
        scanParams.count(ServerConstant.PAGE_NUM);
        scanParams.match("*");
        ScanResult<String> scan = jedis.scan("0", scanParams);
        jedis.close();
        List<String> result = scan.getResult();
        List<String> keys = new ArrayList<>();
        keys.addAll(result);
        if (!scan.getStringCursor().equals("0")) {
            new Thread(() -> {
                String cursor = scan.getStringCursor();
                Jedis resource = jedisPool.getResource();
                while (!cursor.equals("0")) {
                    ScanResult<String> scan1 = resource.scan(cursor, scanParams);
                    cursor = scan1.getStringCursor();
                    keys.addAll(scan1.getResult());
                }
                dbKeysMap.put(db,keys);
                resource.close();
            }).start();
        }
        return result;
    }

    public Set<String> keys() {
        Jedis jedis = JedisFactory.getJedis();
        jedis.select(0);
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
