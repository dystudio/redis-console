package com.whe.redis.domain;

/**
 * Created by trustme on 2017/3/22.
 */
public class RedisInfo {
    private String host;
    private Integer port;
    private String password;
    private String name;
    private String masterName;
    public RedisInfo(){}
    public RedisInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RedisInfo(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public RedisInfo(String host, Integer port, String password, String name) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.name = name;
    }

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



    @Override
    public String toString() {
        return "RedisInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
