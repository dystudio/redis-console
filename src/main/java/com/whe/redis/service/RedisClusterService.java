package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.RedisClusterUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Created by wang hongen on 2017/2/24.
 * RedisClusterService
 */
@Service
public class RedisClusterService {
    private ScanParams scanParams = new ScanParams();



    public ScanResult<String> scan(String cursor) {
        JedisCluster jedisCluster = JedisFactory.getJedisCluster();
        ScanParams count = scanParams.count(ServerConstant.PAGE_NUM);
        ScanParams match = scanParams.match(ServerConstant.DEFAULT_MATCH);
        System.out.println(scanParams);
        return jedisCluster.scan(cursor, match);
    }

    public Map<String, String> getType(List<String> keys) {
        JedisCluster jedisCluster = JedisFactory.getJedisCluster();
        return keys.stream().collect(toMap(key -> key, jedisCluster::type));
    }

    /**
     * 删除所有数据
     */
    public void flushAll() {
        JedisCluster jedisCluster = JedisFactory.getJedisCluster();
        //检查集群节点是否发生变化
        RedisClusterUtils.checkClusterChange(jedisCluster);
        jedisCluster.getClusterNodes().forEach((key, pool) -> {
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
