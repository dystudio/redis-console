package com.whe.redis.conf;


import com.alibaba.fastjson.JSON;
import com.whe.redis.domain.RedisInfo;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RedisConf {
    private final static String confPath = System.getProperty("user.home") + "/redisConsole";
    private final static String confName = "redis.conf";
    private File file = new File(confPath, confName);

    /**
     * 添加新节点 保存到配置文件
     *
     * @param redisInfo RedisInfo
     * @throws Exception
     */
    public void addConf(RedisInfo redisInfo) throws Exception {
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
                Object obj = JSON.parse(jsonBuff.toString());
                if (obj instanceof Map) {
                    Map<String, List<RedisInfo>> infoMap = (Map) obj;
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
                }

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

}
