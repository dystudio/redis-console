package com.whe.redis.service;

import com.alibaba.fastjson.JSON;
import com.whe.redis.cluster.ClusterPipeline;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.SerializeUtils;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Created by wang hongen on 2017/2/24.
 * RedisClusterService
 */
@Service
public class ClusterService {
    private ScanParams scanParams = new ScanParams();
    private JedisCluster jedisCluster;
    private ClusterPipeline clusterPipeline;

    {
        scanParams.match(ServerConstant.DEFAULT_MATCH);
        jedisCluster = JedisFactory.getJedisCluster();
        clusterPipeline = new ClusterPipeline(jedisCluster, JedisFactory.getClusterInfoCache());
    }

    public Long hSetNxSerialize(String key, String field, String value) {
        return jedisCluster.hsetnx(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(value));
    }

    public Long hSetNx(String key, String field, String value) {
        return jedisCluster.hsetnx(key, field, value);
    }

    public Long zAddSerialize(String key, Double score, String member) {
        Boolean exists = jedisCluster.exists(key);
        if (exists && !jedisCluster.type(key).equals(ServerConstant.REDIS_ZSET)) {
            return 2L;
        }
        jedisCluster.zadd(key.getBytes(), score, SerializeUtils.serialize(member));
        return 1L;
    }

    public Long zAdd(String key, Double score, String member) {
        Boolean exists = jedisCluster.exists(key);
        if (exists && !jedisCluster.type(key).equals(ServerConstant.REDIS_ZSET)) {
            return 2L;
        }
        jedisCluster.zadd(key, score, member);
        return 1L;
    }

    public Long sAddSerialize(String key, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists && !jedisCluster.type(key).equals(ServerConstant.REDIS_SET)) {
            return 2L;
        }
        jedisCluster.sadd(key.getBytes(), SerializeUtils.serialize(value));
        return 1L;
    }

    public Long sAdd(String key, String value) {
        Boolean exists = jedisCluster.exists(key);
        if (exists && !jedisCluster.type(key).equals(ServerConstant.REDIS_SET)) {
            return 2L;
        }
        jedisCluster.sadd(key, value);
        return 1L;
    }

    public Long lPushSerialize(String key, String value) {
        jedisCluster.lpush(key.getBytes(), SerializeUtils.serialize(value));
        return 1L;
    }

    public Long lPush(String key, String value) {
        jedisCluster.lpush(key, value);
        return 1L;
    }

    public Long setNxSerialize(String key, String value) {
        return jedisCluster.setnx(key.getBytes(), SerializeUtils.serialize(value));
    }

    public Long setNx(String key, String value) {
        return jedisCluster.setnx(key, value);
    }

    /**
     * 序列化保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHashSerialize(Map<String, Map<String, String>> hashMap) {
        hashMap.forEach((key, map) -> map.forEach((field, val) -> clusterPipeline.hSet(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val))));
        clusterPipeline.sync();
    }

    /**
     * 序列化保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSetSerialize(Map<String, Map<String, Number>> zSetMap) {
        zSetMap.forEach((key, map) -> map.forEach((elem, score) -> clusterPipeline.zAdd(key.getBytes(), score.doubleValue(), SerializeUtils.serialize(elem))));
        clusterPipeline.sync();
    }

    /**
     * 序列化保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSetSerialize(Map<String, List<String>> setMap) {
        setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> clusterPipeline.sAdd(key.getBytes(), SerializeUtils.serialize(val))));
        clusterPipeline.sync();
    }

    /**
     * 序列化保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllListSerialize(Map<String, List<String>> listMap) {
        listMap.forEach((key, list) -> list.forEach(val -> clusterPipeline.lPush(key.getBytes(), SerializeUtils.serialize(val))));
        clusterPipeline.sync();
    }

    /**
     * 序列化保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllStringSerialize(Map<String, String> stringMap) {
        stringMap.forEach((key, val) -> clusterPipeline.set(key.getBytes(), val.getBytes()));
        clusterPipeline.sync();
    }

    /**
     * 保存所有hash数据
     *
     * @param hashMap map
     */
    public void saveAllHash(Map<String, Map<String, String>> hashMap) {
        hashMap.forEach((key, map) -> map.forEach((field, val) -> clusterPipeline.hSet(key, field, val)));
        clusterPipeline.sync();
    }


    /**
     * 保存所有zSet数据
     *
     * @param zSetMap map
     */
    public void saveAllZSet(Map<String, Map<String, Number>> zSetMap) {
        zSetMap.forEach((key, map) -> map.forEach((elem, score) -> clusterPipeline.zAdd(key, score.doubleValue(), elem)));
        clusterPipeline.sync();
    }

    /**
     * 保存所有set数据
     *
     * @param setMap map
     */
    public void saveAllSet(Map<String, List<String>> setMap) {
        setMap.forEach((key, list) -> new HashSet<>(list).forEach(val -> clusterPipeline.sAdd(key, val)));
        clusterPipeline.sync();
    }

    /**
     * 保存所有list数据
     *
     * @param listMap map
     */
    public void saveAllList(Map<String, List<String>> listMap) {
        listMap.forEach((key, list) -> list.forEach(val -> clusterPipeline.lPush(key, val)));
        clusterPipeline.sync();
    }

    /**
     * 保存所有string数据
     *
     * @param stringMap map
     */
    public void saveAllString(Map<String, String> stringMap) {
        stringMap.forEach(clusterPipeline::set);
        clusterPipeline.sync();
    }

    /**
     * 获得所有数据
     */
    public String backup() {

        //管道获得所有key
        Set<String> keys = jedisCluster.getClusterNodes()
                .entrySet()
                .stream()
                .filter(entry -> JedisFactory.getClusterInfoCache().getMasterNode().contains(entry.getKey()))
                .map(entry -> {
                    try (Jedis jedis = entry.getValue().getResource()) {
                        Pipeline pipeline = jedis.pipelined();
                        Response<Set<String>> responseKeys = pipeline.keys(ServerConstant.DEFAULT_MATCH);
                        pipeline.sync();
                        return responseKeys.get();
                    }
                })
                .flatMap(Collection::stream)
                .collect(toSet());

        //获得key的类型
        Map<String, Response<String>> keyType = new HashMap<>(keys.size());
        keys.forEach(key -> keyType.put(key, clusterPipeline.type(key)));
        clusterPipeline.sync();

        Map<String, Response<String>> responseString = new HashMap<>();
        Map<String, Response<List<String>>> responseList = new HashMap<>();
        Map<String, Response<Set<String>>> responseSet = new HashMap<>();
        Map<String, Response<Set<Tuple>>> responseTuple = new HashMap<>();
        Map<String, Response<Map<String, String>>> responseHash = new HashMap<>();
        //获得所有数据
        keyType.forEach((key, type) -> {
            if (ServerConstant.REDIS_STRING.equals(type.get())) {
                responseString.put(key, clusterPipeline.get(key));
            } else if (ServerConstant.REDIS_LIST.equals(type.get())) {
                responseList.put(key, clusterPipeline.lRange(key, 0, -1));
            } else if (ServerConstant.REDIS_SET.equals(type.get())) {
                responseSet.put(key, clusterPipeline.sMembers(key));
            } else if (ServerConstant.REDIS_ZSET.equalsIgnoreCase(type.get())) {
                responseTuple.put(key, clusterPipeline.zRangeWithScores(key, 0, -1));
            } else if (ServerConstant.REDIS_HASH.equals(type.get())) {
                responseHash.put(key, clusterPipeline.hGetAll(key));
            }
        });
        clusterPipeline.sync();

        Map<String, String> stringMap = new HashMap<>(responseString.size());
        Map<String, List<String>> listMap = new HashMap<>(responseList.size());
        Map<String, Set<String>> setMap = new HashMap<>(responseSet.size());
        Map<String, Set<Tuple>> tupleMap = new HashMap<>(responseTuple.size());
        Map<String, Map<String, String>> hashMap = new HashMap<>(responseHash.size());

        responseString.forEach((key, val) -> stringMap.put(key, val.get()));
        responseList.forEach((key, val) -> listMap.put(key, val.get()));
        responseSet.forEach((key, val) -> setMap.put(key, val.get()));
        responseTuple.forEach((key, val) -> tupleMap.put(key, val.get()));
        responseHash.forEach((key, val) -> hashMap.put(key, val.get()));

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
            Map<String, Double> stringDoubleMap = new HashMap<>();
            Map<String, Map<String, Double>> zSetMap = new HashMap<>();
            tupleMap.forEach((key, set) -> {
                set.forEach(t -> stringDoubleMap.put(t.getElement(), t.getScore()));
                zSetMap.put(key, new HashMap<>(stringDoubleMap));
                stringDoubleMap.clear();
            });
            map.put(ServerConstant.REDIS_ZSET, zSetMap);
        }
        if (hashMap.size() > 0) {
            map.put(ServerConstant.REDIS_HASH, hashMap);
        }
        return JSON.toJSONString(map);
    }

    public void hSet(String key, String field, String val) {
        jedisCluster.hset(key, field, val);
    }

    public void hSetSerialize(String key, String field, String val) {
        jedisCluster.hset(key.getBytes(), SerializeUtils.serialize(field), SerializeUtils.serialize(val));
    }

    public Boolean updateHash(String key, String oldField, String newField, String val) {
        Boolean hExists = jedisCluster.hexists(key, newField);
        if (hExists) {
            return false;
        }
        jedisCluster.hdel(key, oldField);
        jedisCluster.hset(key, newField, val);
        return true;
    }

    public Boolean updateHashSerialize(String key, String oldField, String newField, String val) {
        Boolean hExists = jedisCluster.hexists(key, newField);
        if (hExists) {
            return false;
        }
        jedisCluster.hdel(key, oldField);
        hSetSerialize(key, newField, val);
        return true;
    }

    public void delHash(String key, String field) {
        jedisCluster.hdel(key, field);
    }

    public Map<String, String> hGetAll(String key) {
        return jedisCluster.hgetAll(key);
    }

    public Map<String, String> hGetAll(byte[] key) {
        return jedisCluster.hgetAll(key)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> SerializeUtils.unSerialize(entry.getKey()).toString(),
                        entry -> SerializeUtils.unSerialize(entry.getValue()).toString()));
    }

    public void delZSet(String key, String val) {
        jedisCluster.zrem(key, val);
    }

    public void updateZSet(String key, String oldVal, String newVal, double score) {
        jedisCluster.zrem(key, oldVal);
        jedisCluster.zadd(key, score, newVal);
    }

    public void updateZSetSerialize(String key, String oldVal, String newVal, double score) {
        jedisCluster.zrem(key, oldVal);
        jedisCluster.zadd(key.getBytes(), score, SerializeUtils.serialize(newVal));
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

    public Page<Set<Tuple>> findZSetPageByKey(byte[] key, int pageNo) {
        Page<Set<Tuple>> page = new Page<>();
        Set<Tuple> tupleSet = jedisCluster.zrangeByScoreWithScores(key,
                (pageNo - 1) * ServerConstant.PAGE_NUM,
                pageNo * ServerConstant.PAGE_NUM)
                .stream()
                .map(tuple -> new Tuple(SerializeUtils.unSerialize(tuple.getBinaryElement()).toString(), tuple.getScore()))
                .collect(toSet());
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

    public void updateSetSerialize(String key, String oldVal, String newVal) {
        jedisCluster.srem(key, oldVal);
        jedisCluster.sadd(key.getBytes(), SerializeUtils.serialize(newVal));
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

    /**
     * 序列化查询set类型数据
     *
     * @return Set<String>
     */
    public Set<String> getSet(byte[] key) {
        return jedisCluster.smembers(key)
                .stream()
                .map(bytes -> SerializeUtils.unSerialize(bytes).toString())
                .collect(toSet());
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

    public void lSetSerialize(int index, String key, String value) {
        jedisCluster.lset(key.getBytes(), index, SerializeUtils.serialize(value));
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

    /**
     * 根据key序列化分页查询list类型数据
     *
     * @return Page<List<String>>
     */
    public Page<List<String>> findListPageByKey(byte[] key, int pageNo) {
        Page<List<String>> page = new Page<>();
        List<String> list = jedisCluster.lrange(key, (pageNo - 1) * ServerConstant.PAGE_NUM,
                pageNo * ServerConstant.PAGE_NUM)
                .stream()
                .map(bytes -> SerializeUtils.unSerialize(bytes).toString())
                .collect(Collectors.toList());
        //总数据
        page.setTotalRecord(jedisCluster.llen(key));
        page.setPageNo(pageNo);
        page.setResults(list);
        return page;
    }

    public void set(String key, String val) {
        jedisCluster.set(key, val);
    }

    public void setSerialize(String key, String val) {
        jedisCluster.set(key.getBytes(), SerializeUtils.serialize(val));
    }

    public String get(String key) {
        return jedisCluster.get(key);
    }

    public String get(byte[] key) {
        return SerializeUtils.unSerialize(jedisCluster.get(key)).toString();
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

    public void persist(String key) {
        jedisCluster.persist(key);
    }

    public long ttl(String key) {
        return jedisCluster.ttl(key);
    }

    /**
     * 扫描集群key
     *
     * @param nodeCursor Map<String, String> key-> 节点host:port,val->游标 cursor;
     * @param match      匹配词
     * @return Map<String, ScanResult<String>>
     */
    public Map<String, ScanResult<String>> scan(Map<String, String> nodeCursor, String match) {
        Map<String, ScanResult<String>> nodeScan = new HashMap<>(ServerConstant.PAGE_NUM * JedisFactory.getClusterInfoCache().getMasterNode().size());
        int count = ServerConstant.PAGE_NUM / JedisFactory.getClusterInfoCache().getMasterNode().size();
        jedisCluster.getClusterNodes()
                .entrySet()
                .stream()
                .filter(entry -> JedisFactory.getClusterInfoCache().getMasterNode().contains(entry.getKey()))
                .forEach((Map.Entry<String, JedisPool> entry) -> {
                    Jedis jedis = null;
                    try {
                        jedis = entry.getValue().getResource();
                        String cursor = nodeCursor.get(entry.getKey());
                        //第一页 第一次扫描
                        if (cursor == null) {
                            nodeCursor.put(entry.getKey(), ServerConstant.DEFAULT_CURSOR);
                            cursor = nodeCursor.get(entry.getKey());
                        }
                        ScanResult<String> scan = jedis.scan(cursor, scanParams);

                        //当前节点key不够一次扫描key数量 下个节点补上缺的数量
                        if (scan.getResult().size() < count) {
                            scanParams.count(count - scan.getResult().size() + count);
                        } else {
                            scanParams.count(count);
                        }

                        //全部匹配 只扫描一次
                        if (match == null || match.equals(ServerConstant.DEFAULT_MATCH)) {
                            scanParams.match(ServerConstant.DEFAULT_MATCH);
                            nodeScan.put(entry.getKey(), scan);
                            return;
                        }

                        /*
                         对元素的模式匹配工作是在命令从数据集中取出元素之后,向客户端返回元素之前的这段时间内进行的,
                         所以如果被迭代的数据集中只有少量元素和模式相匹配,那么迭代命令或许会在多次执行中都不返回任何元素
                         可能存在大部分迭代都不返回任何元素,扫描到足够数量或迭代完
                         */
                        scanParams.match("*" + match + "*");
                        List<String> keys = new ArrayList<>(count);
                        do {
                            scan = jedis.scan(cursor, scanParams);
                            cursor = scan.getStringCursor();
                            keys.addAll(scan.getResult());
                        } while (!cursor.equals(ServerConstant.DEFAULT_CURSOR) && !(keys.size() >= count));
                        nodeScan.put(entry.getKey(), new ScanResult<>(cursor, keys));
                    } catch (JedisConnectionException e) {
                        JedisFactory.getClusterInfoCache().initializeSlotsCache(jedisCluster);
                    } finally {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
                });
        return nodeScan;
    }

    /**
     * 查询key数据类型
     *
     * @param keys List<String>
     * @return Map<String, String> key->key,val->对应key数据类型
     */
    public Map<String, String> getType(List<String> keys) {
        //集群管道
        keys.forEach(key -> clusterPipeline.type(key));
        Map<String, Response<String>> responseMap = new HashMap<>(keys.size());
        keys.forEach((key) -> responseMap.put(key, clusterPipeline.type(key)));
        //执行
        clusterPipeline.sync();
        Map<String, String> typeMap = new HashMap<>(responseMap.size());
        responseMap.forEach((key, val) -> typeMap.put(key, val.get()));
        return typeMap;
    }


    /**
     * 删除所有数据
     */
    public void flushAll() {
        //删除所有master节点数据 slave会同步master
        jedisCluster.getClusterNodes().forEach((key, pool) -> {
            if (JedisFactory.getClusterInfoCache().getMasterNode().contains(key)) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.flushAll();
                }
            }
        });
    }
}
