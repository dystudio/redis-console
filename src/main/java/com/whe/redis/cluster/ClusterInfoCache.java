package com.whe.redis.cluster;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

/**
 * Created by wang hongen on 2017/1/23.
 * ClusterInfoCache
 */
public class ClusterInfoCache {
    private Set<String> masterNode = new HashSet<>();
    private Set<String> slaveNode = new HashSet<>();
    private TreeMap<Long, Pipeline> slotPipelineMap = new TreeMap<>();
    private Map<String, JedisPool> nodeMap;
    private static final int MASTER_NODE_INDEX = 2;
    private static final int SLAVE_NODE_INDEX = 3;

    public ClusterInfoCache(JedisCluster jedisCluster) {
        initializeSlotsCache(jedisCluster);
    }

    public void initializeSlotsCache(JedisCluster jedisCluster) {
        this.nodeMap = jedisCluster.getClusterNodes();
        for (Map.Entry<String, JedisPool> entry : nodeMap.entrySet()) {
            Jedis jedis = null;
            try {
                jedis = entry.getValue().getResource();
                initialSlotNode(jedis);
            } catch (Exception e) {
                // try next nodes
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

    }


    private void initialSlotNode(Jedis jedis) {
        slotPipelineMap.clear();
        masterNode.clear();
        slaveNode.clear();

        List<Object> slots = jedis.clusterSlots();
        for (Object slotInfoObj : slots) {
            List<Object> slotInfo = (List<Object>) slotInfoObj;
            List<Object> master = (List<Object>) slotInfo.get(MASTER_NODE_INDEX);
            String node = new String((byte[]) master.get(0)) + ":" + master.get(1);
            masterNode.add(node);
            try (Jedis resource = this.nodeMap.get(node).getResource()) {
                Pipeline pipeline = resource.pipelined();
                slotPipelineMap.put((Long) slotInfo.get(0),pipeline);
                slotPipelineMap.put((Long) slotInfo.get(1),pipeline);
            }
            List<Object> slave = (List<Object>) slotInfo.get(SLAVE_NODE_INDEX);
            String slaveNode = new String((byte[]) slave.get(0)) + ":" + slave.get(1);
            this.slaveNode.add(slaveNode);
        }
    }

    Pipeline getPipelineByKey(String key) {
        //获取槽号
        int slot = JedisClusterCRC16.getSlot(key) + 1;
        return slotPipelineMap.lowerEntry((long) slot).getValue();
    }

    public Set<String> getMasterNode() {
        return masterNode;
    }

    public Set<String> getSlaveNode() {
        return slaveNode;
    }

    TreeMap<Long, Pipeline> getSlotPipelineMap() {
        return slotPipelineMap;
    }

}
