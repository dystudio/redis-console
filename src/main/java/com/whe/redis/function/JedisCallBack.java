package com.whe.redis.function;

import redis.clients.jedis.Jedis;

/**
 * Created by wang hongen on 2017/3/9.
 * JedisCallBack
 */
@FunctionalInterface
public interface JedisCallBack<T> {
    T doInRedis(Jedis jedis) throws Exception;
}
