package com.whe.redis.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wang hongen on 2017/1/23.
 * RedisClusterNode
 */
public class RedisClusterNode {
    private Set<String> nodeInfoSet = new HashSet<>();
    private Set<String> masterNodeInfoSet = new HashSet<>();
    private Set<String> slaveNodeInfoSet = new HashSet<>();

    public RedisClusterNode(String clusterNodes) {
        String[] split = clusterNodes.split("\n");
        for (String nodeInfo : split) {
            nodeInfoSet.add(nodeInfo);
            String[] node = nodeInfo.split(" ");
            if (node.length > 3) {
                String[] role = node[2].split(",");
                boolean isMaster;
                if (role.length >= 2) {
                    isMaster = "master".equalsIgnoreCase(role[1]);

                } else {
                    isMaster = "master".equalsIgnoreCase(role[0]);
                }
                if (isMaster) {
                    masterNodeInfoSet.add(node[1]);
                } else {
                    slaveNodeInfoSet.add(node[1]);
                }
            }
        }

    }

    public Set<String> getMasterNodeInfoSet() {
        return masterNodeInfoSet;
    }

    public void setMasterNodeInfoSet(Set<String> masterNodeInfoSet) {
        this.masterNodeInfoSet = masterNodeInfoSet;
    }

    public Set<String> getNodeInfoSet() {
        return nodeInfoSet;
    }

    public void setNodeInfoSet(Set<String> nodeInfoSet) {
        this.nodeInfoSet = nodeInfoSet;
    }

    public Set<String> getSlaveNodeInfoSet() {
        return slaveNodeInfoSet;
    }

    public void setSlaveNodeInfoSet(Set<String> slaveNodeInfoSet) {
        this.slaveNodeInfoSet = slaveNodeInfoSet;
    }
}
