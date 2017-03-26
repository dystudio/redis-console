package com.whe.redis.conf;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whe.redis.domain.RedisInfo;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class RedisConf {
    private final String confPath = System.getProperty("user.home") + "/redisConsole";
    private final String confName = "redis.conf";
    private final File file = new File(confPath, confName);
    private static RedisConf redisConf = null;

    /**
     * 添加新节点 保存到配置文件
     *
     * @param redisInfo RedisInfo
     * @throws Exception
     */
    public void addConf(RedisInfo redisInfo) throws Exception {
        Optional<Map<String, List<RedisInfo>>> mapOptional = readConf();
        //文件是否存在
        if (mapOptional.isPresent()) {
            Map<String, List<RedisInfo>> infoMap = mapOptional.get();
            List<RedisInfo> infoList = infoMap.get(redisInfo.getServerType());
            //添加配置文件
            if (infoList == null) {
                infoList = new ArrayList<>(2);
                infoList.add(redisInfo);
            } else {

                infoList.add(redisInfo);
            }
            infoMap.put(redisInfo.getServerType(), infoList);
            String jsonString = JSON.toJSONString(infoMap);
            //保存写出
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                //写出配置文件
                bw.write(jsonString);
            }
        } else {
            //不存在创建 目录
            file.getParentFile().mkdirs();
            Map<String, List<RedisInfo>> infoMap = new HashMap<>(1, 2);
            List<RedisInfo> infoList = new ArrayList<>(2);
            infoList.add(redisInfo);
            infoMap.put(redisInfo.getServerType(), infoList);
            String jsonString = JSON.toJSONString(infoMap);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                //写出配置文件
                bw.write(jsonString);
            }
        }
    }

    public Optional<Map<String, List<RedisInfo>>> readConf() {
        try {
            //文件是否存在
            if (file.exists()) {
                // 文件存在  读取
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    StringBuilder jsonBuff = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonBuff.append(line);
                    }
                    //把读取到JSON串转成对象
                    if (jsonBuff.toString().equals("")) return Optional.empty();
                    Map<String, List<JSONObject>> map = (Map<String, List<JSONObject>>) JSON.parse(jsonBuff.toString());
                    Map<String, List<RedisInfo>> infoMap = map.entrySet()
                            .stream()
                            .collect(toMap(Map.Entry::getKey,
                                    entry -> entry.getValue()
                                            .stream()
                                            .map(jsonObject -> {
                                                RedisInfo redisInfo = new RedisInfo();
                                                BeanWrapper beanWrapper = new BeanWrapperImpl(redisInfo);
                                                beanWrapper.setPropertyValues(jsonObject);
                                                return redisInfo;
                                            }).collect(toList())
                            ));

                    return Optional.of(infoMap);
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RedisConf getInstance() {
        if (redisConf == null) {
            synchronized (RedisConf.class) {
                if (redisConf == null) {
                    redisConf = new RedisConf();
                }
            }
        }
        return redisConf;
    }
}
