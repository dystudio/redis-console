package com.whe.redis.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wang hongen on 2017/1/23.
 * ClusterNode
 */
public class ClusterNode {
    private Set<String> masterNode = new HashSet<>();
    private Set<String> slaveNode = new HashSet<>();
    private Set<String> clusterNode = new HashSet<>();
    private static final int HOST_AND_PORT_INDEX = 1;
    private static final String MASTER = "master";
    private static final String FAIL = "fail";

    public ClusterNode(String nowNode, String clusterNodes) {
        parse(nowNode, clusterNodes);

    }

    private void parse(String nowNode, String clusterNodes) {
        String[] split = clusterNodes.split("\\n");
        for (String nodeInfo : split) {
            String[] node = nodeInfo.split(" ");
            if (node.length > 3) {
                String[] role = node[2].split(",");
                boolean isMaster;
                if (role.length >= 2) {
                    isMaster = MASTER.equalsIgnoreCase(role[1]);
                    if (FAIL.equalsIgnoreCase(role[1])) {
                        continue;
                    }
                    node[1] = nowNode;
                } else {
                    isMaster = MASTER.equalsIgnoreCase(role[0]);

                }
                if (isMaster) {
                    masterNode.add(node[HOST_AND_PORT_INDEX]);
                } else {
                    slaveNode.add(node[HOST_AND_PORT_INDEX]);
                }
                clusterNode.add(node[1]);
            }
        }
    }

    public Set<String> getMasterNode() {
        return masterNode;
    }

    public Set<String> getSlaveNode() {
        return slaveNode;
    }

}
