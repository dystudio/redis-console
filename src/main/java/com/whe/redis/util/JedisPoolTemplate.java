package com.whe.redis.util;

import com.whe.redis.function.JedisCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by wang hongen on 2017/3/9.
 * JedisPoolTemplate
 */
public class JedisPoolTemplate {
    private static final Logger log = LoggerFactory.getLogger(JedisFactory.class);

    private JedisPool JedisPool;

    public JedisPoolTemplate(JedisPool jedisPool) {
        this.JedisPool = jedisPool;
    }

    public <T> T execute(int db,JedisCallBack<T> action ) {
        try (Jedis jedis = JedisPool.getResource()) {
            jedis.select(db);
            return action.doInRedis(jedis);
        } catch (Exception e) {
            log.error("jedisHandler error", e);
            return null;
        }
    }
}
