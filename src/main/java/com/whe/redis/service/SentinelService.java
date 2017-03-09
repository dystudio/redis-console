package com.whe.redis.service;

import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wang hongen on 2017/3/9.
 * SentinelService
 */
@Service
public class SentinelService extends RedisService {
    private JedisSentinelPool jedisSentinelPool = JedisFactory.getJedisSentinelPool();
    private JedisSentinelPoolTemplate sentinelPoolTemplate = new JedisSentinelPoolTemplate(jedisSentinelPool);

    //Hash

    public Long hSetNxSerialize(int db, String key, String field, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.hSetNxSerialize(jedis, db, key, field, value);
        }
    }

    public Long hSetNx(int db, String key, String field, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.hSetNx(jedis, db, key, field, value);
        }
    }

    public Map<String, String> hGetAll(int db, String key) {
        return sentinelPoolTemplate.execute(db, jedis -> jedis.hgetAll(key));
    }

    public Map<String, String> hGetAllSerialize(int db, String key) throws UnsupportedEncodingException {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.hGetAllSerialize(jedis, db, key);
        }
    }

    public void hSet(int db, String key, String field, String val) {
        sentinelPoolTemplate.execute(db, jedis -> jedis.hdel(key, field));
    }

    public boolean updateHash(int db, String key, String oldField, String newField, String val) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.updateHash(jedis, db, key, oldField, newField, val);
        }
    }

    public void delHash(int db, String key, String field) {
        sentinelPoolTemplate.execute(db, jedis -> jedis.hdel(key, field));
    }

    /**
     * 序列化保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHashSerialize(int db, Map<String, Map<String, String>> hashMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
        }
    }


    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(int db, Map<String, Map<String, String>> hashMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            hashMap.forEach((key, map) -> map.forEach((field, val) -> jedis.hset(key, field, val)));
        }
    }

    //zSet

    public Long zAdd(int db, String key, double score, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.zAdd(jedis, db, key, score, value);
        }
    }

    public Long zAddSerialize(int db, String key, double score, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.zAddSerialize(jedis, db, key, score, value);
        }
    }

    /**
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    public Page<Set<Tuple>> findZSetPageByKey(int db, int pageNo, String key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.findZSetPageByKey(jedis, db, pageNo, key);
        }
    }

    public Page<Set<Tuple>> findZSetPageByKeySerialize(int db, String key, int pageNo) throws UnsupportedEncodingException {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.findZSetPageByKeySerialize(jedis, db, key, pageNo);
        }
    }


    public void updateZSet(int db, String key, String oldVal, String newVal, double score) {
        sentinelPoolTemplate.execute(db, jedis -> {
            jedis.zrem(key, oldVal);
            return jedis.zadd(key, score, newVal);
        });
    }

    public void delZSet(int db, String key, String val) {
        sentinelPoolTemplate.execute(db, jedis -> jedis.zrem(key));
    }


    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(int db, Map<String, Map<String, Double>> zSetMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key, score.doubleValue(), elem)));
        }
    }

    /**
     * 序列化保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSetSerialize(int db, Map<String, Map<String, Number>> zSetMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> jedis.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
        }
    }

    public Long sAddSerialize(int db, String key, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.sAddSerialize(jedis, db, key, value);
        }
    }

    public Long sAdd(int db, String key, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.sAdd(jedis, db, key, value);
        }
    }

    /**
     * 查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(int db, String key) {
        return sentinelPoolTemplate.execute(db, jedis -> jedis.smembers(key));
    }

    public Set<String> getSetSerialize(int db, String key) {
        return sentinelPoolTemplate.execute(db, jedis ->
                jedis.smembers(key.getBytes(ServerConstant.CHARSET))
                        .stream()
                        .map(bytes -> SerializeUtils.unSerialize(bytes).toString())
                        .collect(Collectors.toSet()));
    }

    public void updateSet(int db, String key, String oldVal, String newVal) {
        sentinelPoolTemplate.execute(db, jedis -> {
            jedis.srem(key, oldVal);
            return jedis.sadd(key, newVal);
        });
    }

    public void delSet(int db, String key, String val) {
        sentinelPoolTemplate.execute(db, jeids -> jeids.srem(key));
    }

    /**
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(int db, Map<String, List<String>> setMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key, val)));
        }
    }

    /**
     * 序列化保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSetSerialize(int db, Map<String, List<String>> setMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> jedis.sadd(key.getBytes(), SerializeUtils.serialize(val))));
        }
    }


    public Long lPushSerialize(int db, String key, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.lPushSerialize(jedis, db, key, value);
        }
    }

    public Long lPush(int db, String key, String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.lPush(jedis, db, key, value);
        }
    }

    /**
     * 根据key分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    public Page<List<String>> findListPageByKey(int db, String key, int pageNo) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.findListPageByKey(jedis, db, key, pageNo);
        }
    }

    public Page<List<String>> findListPageByKeySerialize(int db, String key, int pageNo) throws UnsupportedEncodingException {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.findListPageByKeySerialize(jedis, db, key, pageNo);
        }
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
        sentinelPoolTemplate.execute(db, jedis -> jedis.lset(key, index, value));
    }

    public long lLen(int db, String key) {
        return sentinelPoolTemplate.execute(db, jedis -> jedis.llen(key));
    }

    public void lRem(int db, int index, String key) {
        sentinelPoolTemplate.execute(db, jedis -> {
            String uuid = UUID.randomUUID().toString();
            jedis.lset(key, index, uuid);
            return jedis.lrem(key, 0, uuid);
        });
    }

    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(int db, Map<String, List<String>> listMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key, val)));
        }
    }


    /**
     * 序列化保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllListSerialize(int db, Map<String, List<String>> listMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            listMap.forEach((key, list) -> list.forEach(val -> jedis.lpush(key.getBytes(), SerializeUtils.serialize(val))));
        }
    }

    public Long setNxSerialize(int db, String key, String value) {
        return sentinelPoolTemplate.execute(db, (jedis) -> jedis.setnx(key.getBytes(), SerializeUtils.serialize(value)));
    }

    public Long setNx(int db, String key, String value) {
        return sentinelPoolTemplate.execute(db, (jedis) -> jedis.setnx(key, value));
    }

    /**
     * 根据数据库和key获得val
     *
     * @param db  db
     * @param key key
     * @return String
     */
    public String getString(int db, String key) {
        return sentinelPoolTemplate.execute(db, (jedis) -> jedis.get(key));
    }

    public String getStringSerialize(int db, String key) throws UnsupportedEncodingException {
        return SerializeUtils.unSerialize(sentinelPoolTemplate.execute(db, (jedis) ->
                jedis.get(key.getBytes(ServerConstant.CHARSET)
                ))).toString();
    }

    /**
     * 更新val
     *
     * @param db  db
     * @param key key
     * @param val newValue
     */
    public void set(int db, String key, String val) {
        sentinelPoolTemplate.execute(db, jedis -> jedis.set(key, val));
    }


    /**
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(int db, Map<String, String> stringMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            stringMap.forEach(jedis::set);
        }
    }

    /**
     * 序列化保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllStringSerialize(int db, Map<String, String> stringMap) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.select(db);
            stringMap.forEach((key, val) -> jedis.set(key.getBytes(), SerializeUtils.serialize(val)));
        }
    }

    public String backup() {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.backup(jedis);
        }
    }

    public void persist(int db, String key) {
        sentinelPoolTemplate.execute(db, (jedis) -> jedis.persist(key));
    }

    public Integer getDataBasesSize() {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.getDataBasesSize(jedis);
        }
    }

    public ScanResult<String> getKeysByDb(int db, String cursor, String match) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.getKeysByDb(jedis, db, cursor, match);
        }
    }

    /**
     * 更新key
     *
     * @param db     db
     * @param oldKey 旧key
     * @param newKey 新key
     */
    public long renameNx(int db, String oldKey, String newKey) {
        return sentinelPoolTemplate.execute(db, jedis -> jedis.renamenx(oldKey, newKey));

    }

    /**
     * @param db  db
     * @param key key
     * @return long
     */
    public long ttl(int db, String key) {
        return sentinelPoolTemplate.execute(db, jedis -> jedis.ttl(key));
    }

    /**
     * 更新生存时间
     *
     * @param db      db
     * @param key     key
     * @param seconds 秒
     */
    public void setExpire(int db, String key, int seconds) {
        sentinelPoolTemplate.execute(db, jedis -> jedis.expire(key, seconds));
    }

    /**
     * 删除key
     *
     * @param db  db
     * @param key key
     */
    public void delKey(int db, String key) {
        sentinelPoolTemplate.execute(db, jedis -> jedis.del(key));
    }

    public Map<String, String> getType(int db, List<String> keys) {
        return sentinelPoolTemplate.execute(db, jedis -> keys.stream().collect(Collectors.toMap(key -> key, jedis::type)));
    }

    public Map<Integer, Long> getDataBases() {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return super.getDataBases(jedis);
        }
    }


    /**
     * 删除所有数据
     */
    public void flushAll() {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.flushAll();
        }
    }
}
