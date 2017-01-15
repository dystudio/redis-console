package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.whe.redis.service.JedisService;
import com.whe.redis.util.ServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Tuple;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wang hongen on 2017/1/12.
 * Redis控制台
 *
 * @author wanghongen
 */
@Controller
public class JedisController {

    @Autowired
    private JedisService jedisService;

    @RequestMapping(value = {"/index", "/"})
    public String index(Model model) {
        Map<String, String> allString = jedisService.getAllString();
        Map<String, List<String>> allList = jedisService.getAllList();
        Map<String, Set<String>> allSet = jedisService.getAllSet();
        Map<String, Set<Tuple>> allZSet = jedisService.getAllZSet();
        Map<String, Map<String, String>> allHash = jedisService.getAllHash();

        model.addAttribute("string", allString);
        model.addAttribute("list", allList);
        model.addAttribute("set", allSet);
        model.addAttribute("zSet", allZSet);
        model.addAttribute("hash", allHash);

        return "index";
    }

    @RequestMapping(value = {"/string"})
    @ResponseBody
    public Map<String,String> string(Model model) {
     /*   Map<String, String> allString = jedisService.getAllString();

        model.addAttribute("string", allString);*/
        return jedisService.getAllString();
    }

    @RequestMapping(value = {"/list"})
    public String list(Model model) {
        Map<String, List<String>> allList = jedisService.getAllList();

        model.addAttribute("list", allList);

        return "index";
    }

    @RequestMapping(value = {"/set"})
    public String set(Model model) {
        Map<String, Set<String>> allSet = jedisService.getAllSet();
        model.addAttribute("set", allSet);
        return "index";
    }

    @RequestMapping(value = {"/zSet"})
    public String zSet(Model model) {
        Map<String, Set<Tuple>> allZSet = jedisService.getAllZSet();
        model.addAttribute("zSet", allZSet);
        return "index";
    }

    @RequestMapping(value = {"/hash"})
    public String hash(Model model) {
        Map<String, Map<String, String>> allHash = jedisService.getAllHash();
        model.addAttribute("hash", allHash);
        return "index";
    }

    /**
     * 备份数据
     *
     * @param request  request
     * @param response response
     * @throws IOException IOException
     */
    @RequestMapping("/backup")
    public void backup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> stringMap = jedisService.getAllString();
        List<Object> list = new ArrayList<>();
        if (stringMap.size() > 0) {
            map.put(ServerConstant.REDIS_STRING, stringMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, List<String>> listMap = jedisService.getAllList();
        if (listMap.size() > 0) {
            map.put(ServerConstant.REDIS_LIST, listMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, Set<String>> setMap = jedisService.getAllSet();
        if (setMap.size() > 0) {
            map.put(ServerConstant.REDIS_SET, setMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, Set<Tuple>> tupleMap = jedisService.getAllZSet();
        if (tupleMap.size() > 0) {
            Map<String, Map<String, Double>> zSetMap = new HashMap<>();
            Map<String, Double> stringDoubleMap = new HashMap<>();
            tupleMap.forEach((key, set) -> {
                set.forEach(t -> stringDoubleMap.put(t.getElement(), t.getScore()));
                zSetMap.put(key, new HashMap<>(stringDoubleMap));
                stringDoubleMap.clear();
            });
            map.put(ServerConstant.REDIS_ZSET, zSetMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, Map<String, String>> hashMap = jedisService.getAllHash();
        if (hashMap.size() > 0) {
            map.put(ServerConstant.REDIS_HASH, hashMap);
            list.add(map);
        }
        String str = JSON.toJSONString(list);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String format = df.format(new Date());
        response.setContentType(request.getServletContext().getMimeType(format + ".redis"));//设置MIME类型
        response.setHeader("Content-Disposition", "attachment; filename=" + format + ".redis");
        response.getWriter().write(str);
    }

    /**
     * 恢复数据
     *
     * @param file file
     * @return string
     */
    @RequestMapping("/recover")
    @ResponseBody
    public String recover(MultipartFile file) {
        try {
            String data = new String(file.getBytes(), "UTF-8");
            JSONArray jsonArray = JSON.parseArray(data);
            jsonArray.stream().filter(obj -> obj instanceof Map).forEach(obj -> {
                Map map = (Map) obj;
                if (map.containsKey(ServerConstant.REDIS_STRING)) {
                    Map stringMap = (Map) map.get(ServerConstant.REDIS_STRING);
                    jedisService.saveAllString(stringMap);
                }
                if (map.containsKey(ServerConstant.REDIS_LIST)) {
                    Map listMap = (Map) map.get(ServerConstant.REDIS_LIST);
                    jedisService.saveAllList(listMap);
                }
                if (map.containsKey(ServerConstant.REDIS_SET)) {
                    Map setMap = (Map) map.get(ServerConstant.REDIS_SET);
                    jedisService.saveAllSet(setMap);
                }
                if (map.containsKey(ServerConstant.REDIS_ZSET)) {
                    Map zSetMap = (Map) map.get(ServerConstant.REDIS_ZSET);
                    System.out.println(zSetMap);
                    jedisService.saveAllZSet(zSetMap);
                }
                if (map.containsKey(ServerConstant.REDIS_HASH)) {
                    Map hashMap = (Map) map.get(ServerConstant.REDIS_HASH);
                    jedisService.saveAllHash(hashMap);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
        return "1";
    }

    /**
     * 删除所有
     *
     * @return string
     */
    @RequestMapping("/flushAll")
    @ResponseBody
    public String flushAll() {
        jedisService.flushAll();
        return "1";
    }
}
