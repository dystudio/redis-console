package com.whe.redis.web;

import com.whe.redis.util.JedisFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.JedisPool;


/**
 * Created by wang hongen on 2017/2/24.
 *
 * @SpringBootApplication申明让spring boot自动给程序进行必要的配置，等价于以默认属性使用
 * @Configuration，@EnableAutoConfiguration和@ComponentScan
 * @RestController返回json字符串的数据，直接可以编写RESTFul的接口；
 */
@Controller
public class CenterController {

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

}
