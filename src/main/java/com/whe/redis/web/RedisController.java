package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.whe.redis.service.RedisService;
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
public class RedisController {

    @Autowired
    private RedisService jedisService;

    /**
     * 入口 首页
     *
     * @param model model
     * @return index
     */
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

    /**
     * ajax加载string类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/string"})
    @ResponseBody
    public Map<String, String> string() {
        return jedisService.getAllString();
    }

    /**
     * ajax加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/list"})
    @ResponseBody
    public Map<String, List<String>> list() {
        return jedisService.getAllList();
    }

    /**
     * ajax加载set类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/set"})
    @ResponseBody
    public Map<String, Set<String>> set() {
        return jedisService.getAllSet();
    }

    /**
     * ajax加载zSet类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/zSet"})
    @ResponseBody
    public Map<String, Set<Tuple>> zSet() {
        return jedisService.getAllZSet();
    }

    @RequestMapping(value = {"/hash"})
    @ResponseBody
    public Map<String, Map<String, String>> hash() {
        return jedisService.getAllHash();
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
     * 序列化恢复数据
     *
     * @param file file
     * @return string
     */
    @RequestMapping("/serializeRecover")
    @ResponseBody
    public String serializeRecover(MultipartFile file) {
        try {
            String data = new String(file.getBytes(), "UTF-8");
            JSONArray jsonArray = JSON.parseArray(data);
            jsonArray.stream().filter(obj -> obj instanceof Map).forEach(obj -> {
                Map map = (Map) obj;
                if (map.containsKey(ServerConstant.REDIS_STRING)) {
                    Map stringMap = (Map) map.get(ServerConstant.REDIS_STRING);
                    jedisService.saveAllStringSerialize(stringMap);
                }
                if (map.containsKey(ServerConstant.REDIS_LIST)) {
                    Map listMap = (Map) map.get(ServerConstant.REDIS_LIST);
                    jedisService.saveAllListSerialize(listMap);
                }
                if (map.containsKey(ServerConstant.REDIS_SET)) {
                    Map setMap = (Map) map.get(ServerConstant.REDIS_SET);
                    jedisService.saveAllSetSerialize(setMap);
                }
                if (map.containsKey(ServerConstant.REDIS_ZSET)) {
                    Map zSetMap = (Map) map.get(ServerConstant.REDIS_ZSET);
                    jedisService.saveAllZSetSerialize(zSetMap);
                }
                if (map.containsKey(ServerConstant.REDIS_HASH)) {
                    Map hashMap = (Map) map.get(ServerConstant.REDIS_HASH);
                    jedisService.saveAllHashSerialize(hashMap);
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
        try {
            jedisService.flushAll();
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }
}
