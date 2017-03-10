package com.whe.redis.service;

import com.alibaba.fastjson.JSON;
import com.whe.redis.util.Page;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.*;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

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
                .collect(toSet());

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
            Pipeline pipeline = jedis.pipelined();
            jedis.select(i);

            Set<String> keys = new HashSet<>(jedis.dbSize().intValue());

            Response<Set<String>> responseKeys = pipeline.keys(ServerConstant.DEFAULT_MATCH);
            pipeline.sync();
            keys.addAll(responseKeys.get());

            Map<String, Response<String>> responseString = new HashMap<>();
            Map<String, Response<List<String>>> responseList = new HashMap<>();
            Map<String, Response<Set<String>>> responseSet = new HashMap<>();
            Map<String, Response<Set<Tuple>>> responseTuple = new HashMap<>();
            Map<String, Response<Map<String, String>>> responseHash = new HashMap<>();

            Map<String, Response<String>> keyType = new HashMap<>(keys.size());
            keys.forEach(key -> keyType.put(key, pipeline.type(key)));
            pipeline.sync();

            keyType.forEach((key, type) -> {
                if (ServerConstant.REDIS_STRING.equals(type.get())) {
                    responseString.put(key, pipeline.get(key));
                } else if (ServerConstant.REDIS_HASH.equals(type.get())) {
                    responseHash.put(key, pipeline.hgetAll(key));
                } else if (ServerConstant.REDIS_LIST.equals(type.get())) {
                    responseList.put(key, pipeline.lrange(key, 0, -1));
                } else if (ServerConstant.REDIS_SET.equals(type.get())) {
                    responseSet.put(key, pipeline.smembers(key));
                } else if (ServerConstant.REDIS_ZSET.equalsIgnoreCase(type.get())) {
                    responseTuple.put(key, pipeline.zrangeWithScores(key, 0, -1));
                }
            });
            pipeline.sync();
            Map<String, String> stringMap = new HashMap<>(responseString.size());
            Map<String, List<String>> listMap = new HashMap<>(responseList.size());
            Map<String, Set<String>> setMap = new HashMap<>(responseSet.size());
            Map<String, Set<Tuple>> tupleMap = new HashMap<>(responseTuple.size());
            Map<String, Map<String, String>> hashMap = new HashMap<>(responseHash.size());


            Map<String, Object> map = new HashMap<>();

            responseString.forEach((key, val) -> stringMap.put(key, val.get()));
            if (stringMap.size() > 0) {
                map.put(ServerConstant.REDIS_STRING, stringMap);
            }

            responseList.forEach((key, val) -> listMap.put(key, val.get()));
            if (listMap.size() > 0) {
                map.put(ServerConstant.REDIS_LIST, listMap);
            }

            responseSet.forEach((key, val) -> setMap.put(key, val.get()));
            if (setMap.size() > 0) {
                map.put(ServerConstant.REDIS_SET, setMap);
            }

            responseTuple.forEach((key, val) -> tupleMap.put(key, val.get()));
            if (tupleMap.size() > 0) {
                Map<String, Map<String, Double>> zSetMap = tupleMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().collect(Collectors.toMap(Tuple::getElement, Tuple::getScore))));
                map.put(ServerConstant.REDIS_ZSET, zSetMap);
            }

            responseHash.forEach((key, val) -> hashMap.put(key, val.get()));
            if (hashMap.size() > 0) {
                map.put(ServerConstant.REDIS_HASH, hashMap);
            }
            if (map.size() > 0) {
                dbKeys.put(i + "", map);
            }
        }
        return JSON.toJSONString(dbKeys);
    }

    Map<String, String> getType(Jedis jedis, int db, List<String> keys) {
        jedis.select(db);
        Pipeline pipeline = jedis.pipelined();
        Map<String, Response<String>> responseMap = keys.stream().collect(toMap(key -> key, pipeline::type));
        pipeline.sync();
        Map<String, String> typeMap = new HashMap<>(responseMap.size());
        responseMap.forEach((key, val) -> typeMap.put(key, val.get()));
        return typeMap;
    }

    Integer getDataBasesSize(Jedis jedis) {
        List<String> list = jedis.configGet(ServerConstant.DATABASES);
        return Integer.parseInt(list.get(1));
    }

    ScanResult<String> getKeysByDb(Jedis jedis, int db, String cursor, String match) {
        jedis.select(db);
        ScanParams scanParams = new ScanParams();
        scanParams.count(ServerConstant.PAGE_NUM);
        if (cursor == null) {
            cursor = ServerConstant.DEFAULT_CURSOR;
        }
        if (StringUtils.isBlank(match) || match.equals(ServerConstant.DEFAULT_MATCH)) {
            match = ServerConstant.DEFAULT_MATCH;
            scanParams.match(match);
            return jedis.scan(cursor, scanParams);
        }
        match = "*" + match + "*";
        scanParams.match(match);
        ScanResult<String> scan;
        List<String> keys = new ArrayList<>(ServerConstant.PAGE_NUM);
        do {
            scan = jedis.scan(cursor, scanParams);
            cursor = scan.getStringCursor();
            keys.addAll(scan.getResult());
        } while (!cursor.equals(ServerConstant.DEFAULT_CURSOR) && !(keys.size() >= ServerConstant.PAGE_NUM));
        return new ScanResult<>(cursor, keys);
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
