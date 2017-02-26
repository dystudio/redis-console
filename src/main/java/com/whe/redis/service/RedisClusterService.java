package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Created by wang hongen on 2017/2/24.
 * RedisClusterService
 */
@Service
public class RedisClusterService {

    private ScanParams scanParams = new ScanParams();
    private JedisCluster jedisCluster;

    {
        scanParams.match(ServerConstant.DEFAULT_MATCH);
        jedisCluster = JedisFactory.getJedisCluster();
    }

    /**
     * 获得所有string类型数据
     *
     * @return Map<String, String>
     */
    public Map<String, String> getAllString(String pattern) {
       /* jedisCluster.getClusterNodes()
                .entrySet()
                .stream()
                .filter(entry->JedisFactory.getRedisClusterNode().getMasterNode().contains(entry.getKey()))
                .collect(toMap(entry->{
                    entry.
                }))*/
        return null;
    }

    public void hSet(String key, String field, String val) {
        jedisCluster.hset(key, field, val);
    }

    public void updateHash(String key, String oldField, String newField, String val) {
        jedisCluster.hdel(key, oldField);
        jedisCluster.hset(key, newField, val);
    }

    public void delHash(String key, String field) {
        jedisCluster.hdel(key, field);
    }

    public Map<String, String> hGetAll(String key) {
        return jedisCluster.hgetAll(key);
    }

    public void delZSet(String key, String val) {
        jedisCluster.zrem(key, val);
    }

    public void updateZSet(String key, String oldVal, String newVal, double score) {
        jedisCluster.zrem(key, oldVal);
        jedisCluster.zadd(key, score, newVal);
    }

    /**
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    public Page<Set<Tuple>> findZSetPageByKey(int pageNo, String key) {
        Page<Set<Tuple>> page = new Page<>();
        Set<Tuple> tupleSet = jedisCluster.zrangeByScoreWithScores(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
        //总数据
        page.setTotalRecord(jedisCluster.zcard(key));
        page.setPageNo(pageNo);
        page.setResults(tupleSet);
        return page;
    }

    public void updateSet(String key, String oldVal, String newVal) {
        jedisCluster.srem(key, oldVal);
        jedisCluster.sadd(key, newVal);
    }

    public void delSet(String key, String val) {
        jedisCluster.srem(key, val);
    }

    /**
     * 查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(String key) {
        return jedisCluster.smembers(key);
    }

    public void lRem(int index, String key) {
        String uuid = UUID.randomUUID().toString();
        jedisCluster.lset(key, index, uuid);
        jedisCluster.lrem(key, 0, uuid);
    }

    public Long lLen(String key) {
        return jedisCluster.llen(key);
    }

    /**
     * 根据索引更新value
     *
     * @param index index
     * @param key   key
     * @param value value
     */
    public void lSet(int index, String key, String value) {
        jedisCluster.lset(key, index, value);
    }


    /**
     * 根据key分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    public Page<List<String>> findListPageByKey(String key, int pageNo) {
        Page<List<String>> page = new Page<>();
        List<String> list = jedisCluster.lrange(key, (pageNo - 1) * ServerConstant.PAGE_NUM, pageNo * ServerConstant.PAGE_NUM);
        //总数据
        page.setTotalRecord(jedisCluster.llen(key));
        page.setPageNo(pageNo);
        page.setResults(list);
        return page;
    }

    public void set(String key, String val) {
        jedisCluster.set(key, val);
    }

    public String get(String key) {
        return jedisCluster.get(key);
    }

    public Long del(String key) {
        return jedisCluster.del(key);
    }

    public Long renameNx(String oldKey, String newKey) {
        return jedisCluster.renamenx(oldKey, newKey);
    }

    public Long expire(String key, int seconds) {
        return jedisCluster.expire(key, seconds);
    }

    public long ttl(String key) {
        return jedisCluster.ttl(key);
    }

    public Map<String, ScanResult<String>> scan(Map<String, String> nodeCursor) {
        Map<String, ScanResult<String>> nodeScan = new HashMap<>(ServerConstant.PAGE_NUM * JedisFactory.getRedisClusterNode().getMasterNode().size());
        int count = ServerConstant.PAGE_NUM / JedisFactory.getRedisClusterNode().getMasterNode().size();
        System.out.println(JedisFactory.getRedisClusterNode().getMasterNode());
        System.out.println(jedisCluster.getClusterNodes().keySet());
        jedisCluster.getClusterNodes()
                .entrySet()
                .stream()
                .filter(entry -> JedisFactory.getRedisClusterNode().getMasterNode().contains(entry.getKey()))
                .forEach(entry -> {
                    Jedis jedis = null;
                    try {
                        jedis = entry.getValue().getResource();
                        if (ServerConstant.PONG.equalsIgnoreCase(jedis.ping())) {
                            String cursor = nodeCursor.get(entry.getKey());
                            if (cursor == null) {
                                nodeCursor.put(entry.getKey(), ServerConstant.DEFAULT_CURSOR);
                                cursor = nodeCursor.get(entry.getKey());
                            }
                            ScanResult<String> scan = jedis.scan(cursor, scanParams);
                            if (scan.getResult().size() < count) {
                                scanParams.count(count - scan.getResult().size() + count);
                            } else {
                                scanParams.count(count);
                            }
                            nodeScan.put(entry.getKey(), jedis.scan(cursor, scanParams));
                        } else {
                            Set<String> slaveNode = JedisFactory.getRedisClusterNode().getSlaveNode();
                            for (String node : slaveNode) {
                                String[] split = node.split(":");
                                Jedis j = null;
                                try {
                                    j = new Jedis(split[0], Integer.parseInt(split[0]));
                                    if (ServerConstant.PONG.equalsIgnoreCase(j.ping())) {
                                        JedisFactory.setRedisClusterNode(new com.whe.redis.util.RedisClusterNode(node, j.clusterNodes()));
                                        break;
                                    }
                                } finally {
                                    if (j != null) {
                                        j.close();
                                    }
                                }
                            }
                        }
                    } finally {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
                });
        return nodeScan;
    }

    public Map<String, String> getType(List<String> keys) {
        return keys.stream().collect(toMap(key -> key, jedisCluster::type));
    }

    /**
     * 删除所有数据
     */
    public void flushAll() {
        jedisCluster.getClusterNodes().forEach((key, pool) -> {
            if (JedisFactory.getRedisClusterNode().getMasterNode().contains(key)) {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.flushAll();
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
        });
    }
}
