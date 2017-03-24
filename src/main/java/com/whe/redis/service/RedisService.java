package com.whe.redis.service;

import com.whe.redis.conf.RedisConf;
import com.whe.redis.domain.RedisInfo;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class RedisService {
    @Resource
    private RedisConf redisConf;

    /**
     * 添加服务
     *
     * @param redisInfo redisInfo
     * @throws Exception Exception
     */
    public boolean add(RedisInfo redisInfo) throws Exception {
        boolean addSuccess = false;
        if (ServerConstant.STANDALONE.equalsIgnoreCase(redisInfo.getServerType())) {
            addSuccess = JedisFactory.addStandAlone(redisInfo);
        } else if (ServerConstant.STANDALONE.equalsIgnoreCase(redisInfo.getServerType())) {
            addSuccess = JedisFactory.addSentinel(redisInfo);
        } else if (ServerConstant.REDIS_CLUSTER.equalsIgnoreCase(redisInfo.getServerType())) {
            addSuccess = JedisFactory.addCluster(redisInfo);
        }
        //添加失败
        if (!addSuccess) return false;
        //保存配置文件
        redisConf.addConf(redisInfo);
        return true;

    }

}
