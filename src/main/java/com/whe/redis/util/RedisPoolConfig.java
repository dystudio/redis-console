package com.whe.redis.util;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by trustme on 2017/2/19.
 * redisPool配置
 */
public class RedisPoolConfig {
    private static GenericObjectPoolConfig poolConfig = null;
    /**
     * 最大尝试
     */
   public final static int MAX_ATTEMPTS=5;
    /**
     * 连接超时
     */
    public final static int TIMEOUT = 10000;

    /**
     * 连接池中最大连接数。高版本：maxTotal，低版本：maxActive
     */
    private final static int MAX_TOTAL = 30;
    /**
     * 连接池中最大空闲的连接数.
     */
    private final static int MAX_IDLE = 10;
    /**
     * 当连接池资源耗尽时，调用者最大阻塞的时间，超时将跑出异常。单位，毫秒数;默认为-1.表示永不超时。高版本：maxWaitMillis，低版本：maxWait
     */
    private final static long MAX_WAIT_MILLIS = 10000L;
    /**
     * 当调用borrow Object方法时，是否进行有效性检查
     */
    private final static boolean TEST_ON_BORROW = true;
    /**
     * 释放连接的扫描间隔（毫秒）
     */
    private final static long TIME_BETWEEN_EVICTION_RUNS_MILLIS = 30000L;
    /**
     * 连接最小空闲时间
     */
    private final static long MIN_EVICTABLE_IDLE_TIME_MILLIS = 1800000L;
    /**
     * 连接空闲多久后释放, 当空闲时间>该值 且 空闲连接>最大空闲连接数 时直接释放
     */
    private final static long SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 10000L;
    /**
     * 在空闲时检查有效性, 默认false
     */
    private final static boolean TEST_WHILE_IDLE = true;

    private RedisPoolConfig() {
    }

    public static GenericObjectPoolConfig getGenericObjectPoolConfig() {
        if (poolConfig == null) {
            synchronized (RedisPoolConfig.class) {
                if (poolConfig == null) {
                    poolConfig = new GenericObjectPoolConfig();
                    poolConfig.setMaxTotal(MAX_TOTAL);
                    poolConfig.setMinIdle(MAX_IDLE);
                    poolConfig.setMaxWaitMillis(MAX_WAIT_MILLIS);
                    poolConfig.setTestOnBorrow(TEST_ON_BORROW);
                    poolConfig.setTimeBetweenEvictionRunsMillis(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
                    poolConfig.setMinEvictableIdleTimeMillis(MIN_EVICTABLE_IDLE_TIME_MILLIS);
                    poolConfig.setSoftMinEvictableIdleTimeMillis(SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
                    poolConfig.setTestWhileIdle(TEST_WHILE_IDLE);
                }
            }
        }
        return poolConfig;
    }
}
