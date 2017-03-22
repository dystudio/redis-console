package com.whe.redis.web;

import com.whe.redis.domain.RedisInfo;
import com.whe.redis.service.RedisService;
import com.whe.redis.util.JedisFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.io.IOException;


/**
 * Created by wang hongen on 2017/2/24.
 *
 * @SpringBootApplication申明让spring boot自动给程序进行必要的配置，等价于以默认属性使用
 * @Configuration，@EnableAutoConfiguration和@ComponentScan
 * @RestController返回json字符串的数据，直接可以编写RESTFul的接口；
 */
@Controller
public class RedisController {
    @Resource
    private RedisService redisService;

    @RequestMapping("/")
    public String index() {
        JedisPool jedisPool = JedisFactory.getJedisPool();
        if (jedisPool != null) {
            return "forward:/standalone/index";
        } else if (JedisFactory.getJedisSentinelPool() != null) {
            return "forward:/sentinel/index";
        } else if (JedisFactory.getJedisCluster() != null) {
            return "forward:/cluster/index";
        }
        return "index";
    }

    @RequestMapping("/add")
    @ResponseBody
    public void add(RedisInfo redisInfo) {
        try {
            System.out.println(redisInfo);
            redisService.add(redisInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
