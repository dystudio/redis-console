package com.whe.redis.service;

import com.alibaba.fastjson.JSON;
import com.whe.redis.domain.RedisInfo;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.RedisConf;
import com.whe.redis.util.RedisPoolConfig;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.util.Map;


@Service
public class RedisService {
    public void add(RedisInfo redisInfo) throws IOException {
        File file = new File(RedisConf.confPath, RedisConf.confName);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder jsonBuff = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonBuff.append(line);
                }
                System.out.println(jsonBuff.toString());
                Object parse = JSON.parse(jsonBuff.toString());
                System.out.println(parse instanceof Map);
                JedisFactory.addStandAloneNode(redisInfo);
            }
        } else {
            file.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                String jsonString = JSON.toJSONString(redisInfo);
                bw.write(jsonString);
            }
        }
    }

}
