package com.whe.redis.service;

import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wang hongen on 2017/3/9.
 * StandAloneService
 */
@Service
public class StandAloneService extends BaseService {

    //Hash

    public Long hSetNxSerialize(String name, int db, String key, String field, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.hSetNxSerialize(jedis, db, key, field, value);
        }
    }

    public Long hSetNx(String name, int db, String key, String field, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.hSetNx(jedis, db, key, field, value);
        }
    }

    public Map<String, String> hGetAll(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.hgetAll(key);
        }
    }

    public Map<String, String> hGetAllSerialize(String name, int db, String key) throws UnsupportedEncodingException {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.hGetAllSerialize(jedis, db, key);
        }
    }

    public void hSet(String name, int db, String key, String field, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.hset(key, field, val);
        }
    }

    public void hSetSerialize(String name, int db, String key, String field, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val));
        }
    }

    public boolean updateHash(String name, int db, String key, String oldField, String newField, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.updateHash(jedis, db, key, oldField, newField, val);
        }
    }

    public Boolean updateHashSerialize(String name, int db, String key, String oldField, String newField, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.updateHashSerialize(jedis, db, key, oldField, newField, val);
        }
    }

    public void delHash(String name, int db, String key, String field) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.hdel(key, field);
        }
    }

    /**
     * 序列化保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHashSerialize(String name, int db, Map<String, Map<String, String>> hashMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> pipeline.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
            pipeline.sync();
        }
    }


    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(String name, int db, Map<String, Map<String, String>> hashMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            hashMap.forEach((key, map) -> map.forEach((field, val) -> pipeline.hset(key, field, val)));
            pipeline.sync();
        }
    }


    //zSet

    public Long zAdd(String name, int db, String key, double score, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.zAdd(jedis, db, key, score, value);
        }
    }

    public Long zAddSerialize(String name, int db, String key, double score, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.zAddSerialize(jedis, db, key, score, value);
        }
    }

    /**
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    public Page<Set<Tuple>> findZSetPageByKey(String name, int db, int pageNo, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.findZSetPageByKey(jedis, db, pageNo, key);
        }
    }

    public Page<Set<Tuple>> findZSetPageByKeySerialize(String name, int db, String key, int pageNo) throws UnsupportedEncodingException {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.findZSetPageByKeySerialize(jedis, db, key, pageNo);
        }
    }


    public void updateZSet(String name, int db, String key, String oldVal, String newVal, double score) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.zrem(key, oldVal);
            jedis.zadd(key, score, newVal);
        }
    }

    public void updateZSetSerialize(String name, int db, String key, String oldVal, String newVal, double score) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.zrem(key, oldVal);
            jedis.zadd(key.getBytes(), score, SerializeUtils.serialize(newVal));
        }
    }

    public void delZSet(String name, int db, String key, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.zrem(key, val);
        }
    }


    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(String name, int db, Map<String, Map<String, Double>> zSetMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> pipeline.zadd(key, score.doubleValue(), elem)));
            pipeline.sync();
        }
    }

    /**
     * 序列化保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSetSerialize(String name, int db, Map<String, Map<String, Number>> zSetMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            zSetMap.forEach((key, map) -> map.forEach((elem, score) -> pipeline.zadd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
            pipeline.sync();
        }
    }

    public Long sAddSerialize(String name, int db, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.sAddSerialize(jedis, db, key, value);
        }
    }

    public Long sAdd(String name, int db, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.sAdd(jedis, db, key, value);
        }
    }

    /**
     * 查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.smembers(key);
        }
    }

    public Set<String> getSetSerialize(String name, int db, String key) throws UnsupportedEncodingException {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.smembers(key.getBytes(ServerConstant.CHARSET))
                    .stream()
                    .map(bytes -> SerializeUtils.unSerialize(bytes).toString())
                    .collect(Collectors.toSet());
        }
    }

    public void updateSet(String name, int db, String key, String oldVal, String newVal) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.srem(key, oldVal);
            jedis.sadd(key, newVal);
        }

    }

    public void updateSetSerialize(String name, int db, String key, String oldVal, String newVal) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.srem(key, oldVal);
            jedis.sadd(key.getBytes(), SerializeUtils.serialize(newVal));
        }
    }

    public void delSet(String name, int db, String key, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.srem(key, val);
        }
    }

    /**
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(String name, int db, Map<String, List<String>> setMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> pipeline.sadd(key, val)));
            pipeline.sync();
        }
    }

    /**
     * 序列化保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSetSerialize(String name, int db, Map<String, List<String>> setMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> pipeline.sadd(key.getBytes(), SerializeUtils.serialize(val))));
            pipeline.sync();
        }
    }

    public Long lPushSerialize(String name, int db, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.lPushSerialize(jedis, db, key, value);
        }
    }

    public Long lPush(String name, int db, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.lPush(jedis, db, key, value);
        }
    }

    /**
     * 根据key分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    public Page<List<String>> findListPageByKey(String name, int db, String key, int pageNo) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.findListPageByKey(jedis, db, key, pageNo);
        }
    }

    public Page<List<String>> findListPageByKeySerialize(String name, int db, String key, int pageNo) throws UnsupportedEncodingException {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
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
    public void lSet(String name, int db, int index, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.lset(key, index, value);
        }
    }

    public void lSetSerialize(String name, int db, int index, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.lset(key.getBytes(), index, SerializeUtils.serialize(value));
        }
    }

    public long lLen(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.llen(key);
        }
    }

    public void lRem(String name, int db, int index, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            String uuid = UUID.randomUUID().toString();
            jedis.lset(key, index, uuid);
            jedis.lrem(key, 0, uuid);
        }
    }

    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(String name, int db, Map<String, List<String>> listMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            listMap.forEach((key, list) -> list.forEach(val -> pipeline.lpush(key, val)));
            pipeline.sync();
        }
    }


    /**
     * 序列化保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllListSerialize(String name, int db, Map<String, List<String>> listMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            listMap.forEach((key, list) -> list.forEach(val -> pipeline.lpush(key.getBytes(), SerializeUtils.serialize(val))));
            pipeline.sync();
        }
    }

    public Long setNxSerialize(String name, int db, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.setnx(key.getBytes(), SerializeUtils.serialize(value));
        }
    }

    public Long setNx(String name, int db, String key, String value) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.setnx(key, value);
        }
    }

    /**
     * 根据数据库和key获得val
     *
     * @param db  db
     * @param key key
     * @return String
     */
    public String getString(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.get(key);
        }
    }

    public String getStringSerialize(String name, int db, String key) throws UnsupportedEncodingException {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return SerializeUtils.unSerialize(jedis.get(key.getBytes(ServerConstant.CHARSET)
            )).toString();
        }
    }

    /**
     * 更新val
     *
     * @param db  db
     * @param key key
     * @param val newValue
     */
    public void set(String name, int db, String key, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.set(key, val);
        }
    }

    public void setSerialize(String name, int db, String key, String val) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.set(key.getBytes(), SerializeUtils.serialize(val));
        }
    }

    /**
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(String name, int db, Map<String, String> stringMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            stringMap.forEach(pipeline::set);
            pipeline.sync();
        }
    }

    /**
     * 序列化保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllStringSerialize(String name, int db, Map<String, String> stringMap) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            Pipeline pipeline = jedis.pipelined();
            stringMap.forEach((key, val) -> pipeline.set(key.getBytes(), SerializeUtils.serialize(val)));
            pipeline.sync();
        }
    }


    public String backup(String name) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.backup(jedis);
        }
    }

    public void persist(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.persist(key);
        }
    }

    public Integer getDataBasesSize(String name) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.getDataBasesSize(jedis);
        }
    }

    public ScanResult<String> getKeysByDb(String name, int db, String cursor, String match) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
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
    public long renameNx(String name, int db, String oldKey, String newKey) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.renamenx(oldKey, newKey);
        }
    }

    /**
     * @param db  db
     * @param key key
     * @return long
     */
    public long ttl(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            return jedis.ttl(key);
        }
    }

    /**
     * 更新生存时间
     *
     * @param db      db
     * @param key     key
     * @param seconds 秒
     */
    public void setExpire(String name, int db, String key, int seconds) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.expire(key, seconds);
        }
    }

    /**
     * 删除key
     *
     * @param db  db
     * @param key key
     */
    public void delKey(String name, int db, String key) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.select(db);
            jedis.del(key);
        }
    }

    public Map<String, String> getType(String name, int db, List<String> keys) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.getType(jedis, db, keys);
        }
    }

    public Map<Integer, Long> getDataBases(String name) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            return super.getDataBases(jedis);
        }
    }

    /**
     * 删除所有数据
     */
    public void flushAll(String name) {
        try (Jedis jedis = JedisFactory.getJedisPool(name).getResource()) {
            jedis.flushAll();
        }
    }
}
