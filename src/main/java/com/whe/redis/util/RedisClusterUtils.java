package com.whe.redis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wang hongen on 2017/2/3.
 *
 * @author wang hongen
 */
public interface RedisClusterUtils {
    /**
     * 检查集群节点是否发生变化
     *
     * @param jedisCluster JedisCluster
     */
    static void checkClusterChange(JedisCluster jedisCluster) {
        Set<String> nodes = new HashSet<>();
        for (Map.Entry<String, JedisPool> entry : jedisCluster.getClusterNodes().entrySet()) {
            Jedis jedis = entry.getValue().getResource();
            if ("PONG".equalsIgnoreCase(jedis.ping())) {
                String[] split = jedis.clusterNodes().split("\\n");
                for (String str : split) {
                    nodes.add(str.split(" ")[1]);
                }
                Set<String> clusterNodeSet = JedisFactory.getRedisClusterNode().getClusterNodeSet();
                if (nodes.size() == clusterNodeSet.size()) {
                    for (String str : nodes) {
                        if (!clusterNodeSet.contains(str)) {
                            JedisFactory.setRedisClusterNode(new RedisClusterNode(jedis.clusterNodes()));
                        }
                    }
                } else {
                    JedisFactory.setRedisClusterNode(new RedisClusterNode(jedis.clusterNodes()));
                }
                jedis.close();
                break;
            }
        }
    }
}
