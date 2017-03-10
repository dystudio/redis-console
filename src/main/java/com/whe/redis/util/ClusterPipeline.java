package com.whe.redis.util;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.TreeMap;

/**
 * Created by wang hongen on 2017/3/10.
 * ClusterPipeline
 */
public class ClusterPipeline {
    private ClusterInfoCache cache;

    public ClusterPipeline(ClusterInfoCache cache) {
        this.cache = cache;
    }

    public Response<String> type(String key) {
        Pipeline pipeline = cache.getPipelineByKey(key);
        return pipeline.type(key);
    }


    public void sync() {
        TreeMap<Long, Pipeline> pipelineMap = cache.getSlotPipelineMap();
        pipelineMap.values().forEach(Pipeline::sync);
    }

}
