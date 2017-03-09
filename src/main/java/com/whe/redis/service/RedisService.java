package com.whe.redis.service;

import com.alibaba.fastjson.JSON;
import com.whe.redis.util.Page;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by wang hongen on 2017/1/13.
 * RedisService
 */
public class RedisService {

    //Hash

    Long hSetNxSerialize(Jedis jedis, int db, String key, String field, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_HASH.equals(jedis.type(key))) {
            return 2L;
        }
        return jedis.hsetnx(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(value));
    }

    Long hSetNx(Jedis jedis, int db, String key, String field, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_HASH.equals(jedis.type(key))) {
            return 2L;
        }
        return jedis.hsetnx(key, field, value);
    }

    Map<String, String> hGetAllSerialize(Jedis jedis, int db, String key) throws UnsupportedEncodingException {
        jedis.select(db);
        return jedis.hgetAll(key.getBytes(ServerConstant.CHARSET))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> SerializeUtils.unSerialize(entry.getKey()).toString(),
                        entry -> SerializeUtils.unSerialize(entry.getValue()).toString()));
    }

    boolean updateHash(Jedis jedis, int db, String key, String oldField, String newField, String val) {
        jedis.select(db);
        Boolean hExists = jedis.hexists(key, newField);
        if (hExists) {
            return false;
        }
        jedis.hdel(key, oldField);
        jedis.hset(key, newField, val);
        return true;
    }


    //zSet

    Long zAdd(Jedis jedis, int db, String key, double score, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !jedis.type(key).equals(ServerConstant.REDIS_ZSET)) {
            return 2L;
        }
        jedis.zadd(key, score, value);
        return 1L;
    }

    Long zAddSerialize(Jedis jedis, int db, String key, double score, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !jedis.type(key).equals(ServerConstant.REDIS_ZSET)) {
            return 2L;
        }
        jedis.zadd(key.getBytes(), score, SerializeUtils.serialize(value));
        return 1L;
    }

    /**
     * 根据key分页查询zSet类型数据
     *
     * @return Page<Set<Tuple>>
     */
    Page<Set<Tuple>> findZSetPageByKey(Jedis jedis, int db, int pageNo, String key) {
        Page<Set<Tuple>> page = new Page<>();
        jedis.select(db);
        Set<Tuple> tupleSet = jedis.zrangeByScoreWithScores(key,
                (pageNo - 1) * ServerConstant.PAGE_NUM,
                pageNo * ServerConstant.PAGE_NUM);
        //总数据
        page.setTotalRecord(jedis.zcard(key));
        page.setPageNo(pageNo);
        page.setResults(tupleSet);
        return page;
    }

    Page<Set<Tuple>> findZSetPageByKeySerialize(Jedis jedis, int db, String key, int pageNo) throws UnsupportedEncodingException {
        Page<Set<Tuple>> page = new Page<>();
        jedis.select(db);
        Set<Tuple> tupleSet = jedis.zrangeByScoreWithScores(key.getBytes(ServerConstant.CHARSET),
                (pageNo - 1) * ServerConstant.PAGE_NUM,
                pageNo * ServerConstant.PAGE_NUM)
                .stream()
                .map(tuple -> new Tuple(SerializeUtils.unSerialize(tuple.getBinaryElement()).toString(), tuple.getScore()))
                .collect(Collectors.toSet());

        //总数据
        page.setTotalRecord(jedis.llen(key));
        page.setPageNo(pageNo);
        page.setResults(tupleSet);
        return page;
    }

    Long sAddSerialize(Jedis jedis, int db, String key, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_SET.equals(jedis.type(key))) {
            return 2L;
        }
        jedis.sadd(key.getBytes(), SerializeUtils.serialize(value));
        return 1L;
    }

    Long sAdd(Jedis jedis, int db, String key, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_SET.equals(jedis.type(key))) {
            return 2L;
        }
        jedis.sadd(key, value);
        return 1L;
    }

    Long lPushSerialize(Jedis jedis, int db, String key, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_LIST.equals(jedis.type(key))) {
            return 2L;
        }
        jedis.lpushx(key.getBytes(), SerializeUtils.serialize(value));
        return 1L;
    }

    Long lPush(Jedis jedis, int db, String key, String value) {
        jedis.select(db);
        Boolean exists = jedis.exists(key);
        if (exists && !ServerConstant.REDIS_LIST.equals(jedis.type(key))) {
            return 2L;
        }
        jedis.lpushx(key, value);
        return 1L;
    }

    /**
     * 根据key分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    Page<List<String>> findListPageByKey(Jedis jedis, int db, String key, int pageNo) {
        Page<List<String>> page = new Page<>();
        jedis.select(db);
        //总数据
        List<String> list = jedis.lrange(key, (pageNo - 1) * ServerConstant.PAGE_NUM,
                pageNo * ServerConstant.PAGE_NUM);
        page.setTotalRecord(jedis.llen(key));
        page.setPageNo(pageNo);
        page.setResults(list);
        return page;
    }


    Page<List<String>> findListPageByKeySerialize(Jedis jedis, int db, String key, int pageNo) throws UnsupportedEncodingException {
        Page<List<String>> page = new Page<>();
        jedis.select(db);
        //总数据
        List<String> list = jedis.lrange(key.getBytes(ServerConstant.CHARSET),
                (pageNo - 1) * ServerConstant.PAGE_NUM,
                pageNo * ServerConstant.PAGE_NUM)
                .stream()
                .map(bytes -> SerializeUtils.unSerialize(bytes).toString())
                .collect(Collectors.toList());
        page.setTotalRecord(jedis.llen(key));
        page.setPageNo(pageNo);
        page.setResults(list);
        return page;
    }

    String backup(Jedis jedis) {
        int size = Integer.parseInt(jedis.configGet(ServerConstant.DATABASES).get(1));
        Map<String, Object> dbKeys = new HashMap<>();
        for (int i = 0; i < size; i++) {
            jedis.select(i);
            String cursor = ServerConstant.DEFAULT_CURSOR;
            ScanParams scanParams = new ScanParams();
            scanParams.count(1000);
            scanParams.match(ServerConstant.DEFAULT_MATCH);
            List<String> keys = new ArrayList<>(jedis.dbSize().intValue());
            do {
                ScanResult<String> scan = jedis.scan(cursor, scanParams);
                cursor = scan.getStringCursor();
                keys.addAll(scan.getResult());
            } while (!ServerConstant.DEFAULT_CURSOR.equals(cursor));

            Map<String, String> stringMap = new HashMap<>();
            Map<String, List<String>> listMap = new HashMap<>();
            Map<String, Set<String>> setMap = new HashMap<>();
            Map<String, Set<Tuple>> tupleMap = new HashMap<>();
            Map<String, Map<String, String>> hashMap = new HashMap<>();
            keys.forEach(key -> {
                String type = jedis.type(key);
                if (ServerConstant.REDIS_STRING.equals(type)) {
                    stringMap.put(key, jedis.get(key));
                } else if (ServerConstant.REDIS_HASH.equals(type)) {
                    hashMap.put(key, jedis.hgetAll(key));
                } else if (ServerConstant.REDIS_LIST.equals(type)) {
                    listMap.put(key, jedis.lrange(key, 0, -1));
                } else if (ServerConstant.REDIS_SET.equals(type)) {
                    setMap.put(key, jedis.smembers(key));
                } else if (ServerConstant.REDIS_ZSET.equalsIgnoreCase(key)) {
                    tupleMap.put(key, jedis.zrangeWithScores(key, 0, -1));
                }
            });
            Map<String, Object> map = new HashMap<>();
            if (stringMap.size() > 0) {
                map.put(ServerConstant.REDIS_STRING, stringMap);
            }
            if (listMap.size() > 0) {
                map.put(ServerConstant.REDIS_LIST, listMap);
            }
            if (setMap.size() > 0) {
                map.put(ServerConstant.REDIS_SET, setMap);
            }
            if (tupleMap.size() > 0) {
                Map<String, Map<String, Double>> zSetMap = tupleMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().collect(Collectors.toMap(Tuple::getElement, Tuple::getScore))));
                map.put(ServerConstant.REDIS_ZSET, zSetMap);
            }
            if (hashMap.size() > 0) {
                map.put(ServerConstant.REDIS_HASH, hashMap);
            }
            if (map.size() > 0) {
                dbKeys.put(i + "", map);
            }
        }
        return JSON.toJSONString(dbKeys);
    }

    Integer getDataBasesSize(Jedis jedis) {
        List<String> list = jedis.configGet(ServerConstant.DATABASES);
        return Integer.parseInt(list.get(1));
    }

    ScanResult<String> getKeysByDb(Jedis jedis, int db, String cursor, String match) {
        if (StringUtils.isBlank(match)) {
            match = ServerConstant.DEFAULT_MATCH;
        } else {
            match = "*" + match + "*";
        }
        jedis.select(db);
        ScanParams scanParams = new ScanParams();
        scanParams.count(ServerConstant.PAGE_NUM);
        scanParams.match(match);
        if (cursor == null) {
            cursor = ServerConstant.DEFAULT_CURSOR;
        }
        return jedis.scan(cursor, scanParams);
    }


    Map<Integer, Long> getDataBases(Jedis jedis) {
        List<String> list = jedis.configGet(ServerConstant.DATABASES);
        int size = Integer.parseInt(list.get(1));
        return IntStream
                .range(0, size)
                .boxed()
                .collect(Collectors.toMap(i -> i, i -> {
                    jedis.select(i);
                    return jedis.dbSize();
                }));
    }
}
