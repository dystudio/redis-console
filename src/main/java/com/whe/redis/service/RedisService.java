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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
@Service
public class RedisService {

    public ScanResult<String> getKeysByDb(int db, String cursor) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        ScanParams scanParams = new ScanParams();
        scanParams.count(ServerConstant.PAGE_NUM);
        scanParams.match(ServerConstant.DEFAULT_MATCH);
        if (cursor == null) {
            cursor = ServerConstant.DEFAULT_CURSOR;
        }
        ScanResult<String> scan = jedis.scan(cursor, scanParams);
        jedis.close();
        return scan;
    }

    public void getValBykey(Integer db, String key) {
        Optional.ofNullable(db).map(i -> {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            jedis.select(i);
            String type = jedis.type(key);
            if (type.equals(ServerConstant.REDIS_STRING)) {
                jedis.get(key);
                jedis.ttl(key);

            }
            jedis.close();
            return "";
        });
    }

    /**
     * 更新key
     *
     * @param db     db
     * @param oldKey 旧key
     * @param newKey 新key
     */
    public long renameNx(int db, String oldKey, String newKey) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Long aLong = jedis.renamenx(oldKey, newKey);
        jedis.close();
        return aLong;
    }


    /**
     * 更新生存时间
     *
     * @param db      db
     * @param key     key
     * @param seconds 秒
     */
    public void setExpire(int db, String key, int seconds) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.expire(key, seconds);
        jedis.close();
    }
   /**
     * 删除key
     *
     * @param db      db
     * @param key     key
     */
    public void delKey(int db, String key) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        jedis.del(key);
        jedis.close();
    }


    public Map<String, String> getType(int db, List<String> keys) {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(db);
        Map<String, String> map = keys.stream().collect(Collectors.toMap(key -> key, jedis::type));
        jedis.close();
        return map;
    }

    public Set<String> keys() {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        jedis.select(0);
        Set<String> keys = jedis.keys("*");
        jedis.close();
        return keys;
    }

    public Map<Integer, Long> getDataBases() {
        Jedis jedis = JedisFactory.getJedisPool().getResource();
        List<String> list = jedis.configGet(ServerConstant.DATABASES);
        int size = Integer.parseInt(list.get(1));
        Map<Integer, Long> map = IntStream.range(0, size).boxed().collect(Collectors.toMap(i -> i, i -> {
            jedis.select(i);
            Long dbSize = jedis.dbSize();
            return dbSize;
        }));
        jedis.close();
        return map;
    }


    /**
     * 删除所有数据
     */
    public void flushAll() {
        if (ServerConstant.STAND_ALONE.equalsIgnoreCase(ServerConstant.REDIS_TYPE)) {
            Jedis jedis = JedisFactory.getJedisPool().getResource();
            jedis.flushAll();
            jedis.close();
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
