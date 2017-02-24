package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.whe.redis.service.RedisClusterService;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.ServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.ScanResult;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wang hongen on 2017/2/24.
 * RedisClusterController
 */
@Controller
@RequestMapping("/redis-cluster")
public class RedisClusterController {
    @Autowired
    private RedisClusterService redisClusterService;


    @RequestMapping("/index")
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {

        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("text:").append("'").append("redisCluster").append("',");
        sb.append("icon:").append(request.getContextPath()).append("'/img/redis.png',").append("expanded:").append(true).append(",");
        sb.append("nodes:").append("[");
        sb.append("{text:").append("'").append("data").append("',").append("icon:").append(request.getContextPath()).append("'/img/db.png',");
        sb.append("nodes:").append("[");
        ScanResult<String> scan = redisClusterService.scan(ServerConstant.DEFAULT_MATCH);
        Map<String, String> typeMap = redisClusterService.getType(scan.getResult());
        typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(request.getContextPath()).append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
        sb.deleteCharAt(sb.length() - 1).append("]}]}]");
        model.addAttribute("tree",sb.toString());
        return "index";
    }

    /**
     * 删除所有
     *
     * @return string
     */
    @RequestMapping("/flushAll")
    @ResponseBody
    public String flushAll() {
        try {
            redisClusterService.flushAll();
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
        return "1";
    }
}
