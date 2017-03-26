package com.whe.redis.web;

import com.whe.redis.domain.RedisInfo;
import com.whe.redis.service.RedisService;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


/**
 * Created by wang hongen on 2017/2/24.
 */
@Controller
public class RedisController {
    private static final Logger log = LoggerFactory.getLogger(RedisController.class);

    @Resource
    private RedisService redisService;

    @RequestMapping("/")
    public String index() {
        if (JedisFactory.getStandaloneMap() != null) {
            return "forward:/standalone/index";
        } else if (JedisFactory.getSentinelMap() != null) {
            return "forward:/sentinel/index";
        } else if (JedisFactory.getClusterMap() != null) {
            return "forward:/cluster/index";
        }
        return "index";
    }

    @RequestMapping("/add")
    @ResponseBody
    public String add(RedisInfo redisInfo) {
        try {
            boolean containsKey = false;
            if (ServerConstant.STANDALONE.equalsIgnoreCase(redisInfo.getServerType())) {
                containsKey = JedisFactory.getStandaloneMap().containsKey(redisInfo.getName());
            } else if (ServerConstant.SENTINEL.equalsIgnoreCase(redisInfo.getServerType())) {
                containsKey = JedisFactory.getSentinelMap().containsKey(redisInfo.getName());
            } else if (ServerConstant.CLUSTER.equalsIgnoreCase(redisInfo.getServerType())) {
                containsKey = JedisFactory.getClusterMap().containsKey(redisInfo.getName());
            }
            System.out.println(redisInfo);
            if (containsKey) return "Name已存在";
            return redisService.add(redisInfo) ? "1" : "添加服务失败";
        } catch (Exception e) {
            String message;
            if(e.getCause()==null){
                message=e.getMessage();
            }else{
                message=e.getCause().getMessage();
            }
            log.error("添加redis服务失败,redisInfo=" + redisInfo + "," + message, e);
            return message;
        }
    }
}
