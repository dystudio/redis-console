package com.whe.redis.domain;

/**
 * Created by trustme on 2017/3/22.
 */
public class RedisInfo {
    private String host;
    private Integer port;
    private String password;
    private String name;
    private String serverType;
    private String masterName;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    @Override
    public String toString() {
        return "RedisInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", serverType='" + serverType + '\'' +
                ", masterName='" + masterName + '\'' +
                '}';
    }
}
