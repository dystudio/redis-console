package com.whe.redis.cluster;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by wang hongen on 2017/3/10.
 * ClusterPipeline
 */
public class ClusterPipeline {
    private JedisCluster jedisCluster;
    private ClusterInfoCache cache;

    public ClusterPipeline(JedisCluster jedisCluster, ClusterInfoCache cache) {
        this.jedisCluster = jedisCluster;
        this.cache = cache;
    }

    public Response<String> type(String key) {
        Pipeline pipeline = cache.getPipelineByKey(key);
        return pipeline.type(key);
    }

    public Response<String> set(String key, String val) {
        return cache.getPipelineByKey(key).set(key, val);
    }

    public Response<String> set(byte[] key, byte[] val) {
        return cache.getPipelineByKey(new String(key)).set(key, val);
    }

    public Response<Long> lPush(String key, String val) {
        return cache.getPipelineByKey(key).lpush(key, val);
    }

    public Response<Long> lPush(byte[] key, byte[] val) {
        return cache.getPipelineByKey(new String(key)).lpush(key, val);
    }

    public Response<Long> sAdd(String key, String val) {
        return cache.getPipelineByKey(key).sadd(key, val);
    }

    public Response<Long> sAdd(byte[] key, byte[] val) {
        return cache.getPipelineByKey(new String(key)).sadd(key, val);
    }

    public Response<Long> zAdd(String key, double score, String val) {
        return cache.getPipelineByKey(key).zadd(key, score, val);
    }

    public Response<Long> zAdd(byte[] key, double score, byte[] val) {
        return cache.getPipelineByKey(new String(key)).zadd(key, score, val);
    }

    public Response<Long> hSet(String key, String field, String val) {
        return cache.getPipelineByKey(key).hset(key, field, val);
    }

    public Response<Long> hSet(byte[] key, byte[] field, byte[] val) {
        return cache.getPipelineByKey(new String(key)).hset(key, field, val);
    }

    public Response<String> get(String key) {
        return cache.getPipelineByKey(key).get(key);
    }

    public Response<List<String>> lRange(String key, long start, long end) {
        return cache.getPipelineByKey(key).lrange(key, start, end);
    }

    public Response<Set<String>> sMembers(String key) {
        return cache.getPipelineByKey(key).smembers(key);
    }

    public Response<Set<Tuple>> zRangeWithScores(String key, long start, long end) {
        return cache.getPipelineByKey(key).zrangeWithScores(key, start, end);
    }

    public Response<Map<String, String>> hGetAll(String key) {
        return cache.getPipelineByKey(key).hgetAll(key);
    }

    public void sync() {
        TreeMap<Long, Pipeline> pipelineMap = cache.getSlotPipelineMap();
        try {
            pipelineMap.values().stream().distinct().forEach(Pipeline::sync);
        } catch (JedisConnectionException jce) {
            cache.initializeSlotsCache(jedisCluster);
        }
    }

}
