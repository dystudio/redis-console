package com.whe.redis.web;

import com.whe.redis.util.JedisFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by wang hongen on 2017/2/24.
 * CentreController
 */
@Controller
public class CentreController {
    @RequestMapping("/")
    public String index(HttpServletRequest request) {
        JedisPool jedisPool = JedisFactory.getJedisPool();
        if (jedisPool != null) {
            return "forward:" + request.getContextPath() + "/standalone/index";
        } else if (JedisFactory.getJedisCluster() != null) {
            return "forward:" + request.getContextPath() + "/redis-cluster/index";
        }
        return "index";
    }

}
