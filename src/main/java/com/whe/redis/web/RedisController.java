package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.whe.redis.service.*;
import com.whe.redis.util.JedisFactory;
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
    private RedisService redisService;
    @Autowired
    private RedisStringService redisStringService;
    @Autowired
    private RedisListService redisListService;
    @Autowired
    private RedisSetService redisSetService;
    @Autowired
    private RedisZSetService redisZSetService;
    @Autowired
    private RedisHashService redisHashService;
    @RequestMapping("/keys")
    public void keys(){
        redisService.keys();
    }

    @RequestMapping("/scan")
    @ResponseBody
    public List<String> scan(Integer db){
        List<String> keysByDb = redisService.getKeysByDb(db);
        return keysByDb;
    }
    /**
     * 入口 首页
     *
     * @param model model
     * @return index
     */
    @RequestMapping(value = {"/"})
    public String index(Model model, String pattern) {
        Set<String> keys = redisService.keys();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("text:").append("'").append(JedisFactory.getStandAlone()).append("',");
        sb.append("icon:").append("'server',");
        Map<Integer, Long> dataBases = redisService.getDataBases();
        sb.append("nodes:").append("[");
        dataBases.entrySet().forEach(entry -> {
            sb.append("{text:").append("'").append("DB-").append(entry.getKey()).append("',")
                    .append("icon:").append("'redisDb',")
                    .append("tags:").append("['")
                    .append(entry.getValue()).append("']");
            if (entry.getValue() > 0) {
                sb.append(",");
                sb.append("nodes:").append("[");
                keys.forEach(key -> sb.append("{text:").append("'").append(key).append("'},"));
                sb.append("]");
            }
            sb.append("},");
        });
        sb.deleteCharAt(sb.length() - 1).append("]}]");
        model.addAttribute("tree", sb.toString());
       /* Map<String, String> allString = redisStringService.getAllString(pattern);
        Map<String, List<String>> allList = redisListService.getAllList();
        Map<String, Set<String>> allSet = redisSetService.getAllSet();
        Map<String, Set<Tuple>> allZSet = redisZSetService.getAllZSet();
        Map<String, Map<String, String>> allHash = redisHashService.getAllHash();*/
     /*   Page<Map<String, List<String>>> listPage = redisListService.findListPageByQuery(1, "*");
        Page<Map<String, String>> stringPage = redisStringService.findStringPageByQuery(1, pattern);
        Page<Map<String, Set<String>>> setPage = redisSetService.findSetPageByQuery(1, "*");
        Page<Map<String, Set<Tuple>>> zSetPage = redisZSetService.findZSetPageByQuery(1, "*");
        Page<Map<String, Map<String, String>>> hashPage = redisHashService.findHashPageByQuery(1, "*");*/

        Set<String> type = new HashSet<>();
        type.add("string");
        type.add("list");
        type.add("set");
        type.add("zSet");
        type.add("hash");
        model.addAttribute("type", type);

       /* model.addAttribute("stringPage", stringPage);
        model.addAttribute("listPage", listPage);
        model.addAttribute("setPage", setPage);
        model.addAttribute("zSetPage", zSetPage);
        model.addAttribute("hashPage", hashPage);*/
        /*model.addAttribute("string", allString);
        model.addAttribute("list", allList);
        model.addAttribute("set", allSet);
        model.addAttribute("zSet", allZSet);
        model.addAttribute("hash", allHash);*/

        return "index";
    }

    /**
     * ajax加载string类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/string"})
    @ResponseBody
    public Map<String, String> string(String pattern) {
        return redisStringService.getAllString(pattern);
    }

    /**
     * ajax加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/list"})
    @ResponseBody
    public Map<String, List<String>> list() {
        return redisListService.getAllList();
    }

    /**
     * ajax加载set类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/set"})
    @ResponseBody
    public Map<String, Set<String>> set() {
        return redisSetService.getAllSet();
    }

    /**
     * ajax加载zSet类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/zSet"})
    @ResponseBody
    public Map<String, Set<Tuple>> zSet() {
        return redisZSetService.getAllZSet();
    }

    @RequestMapping(value = {"/hash"})
    @ResponseBody
    public Map<String, Map<String, String>> hash() {
        return redisHashService.getAllHash();
    }

    public void keywordQuery() {

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
        Map<String, String> stringMap = redisStringService.getAllString("*");
        List<Object> list = new ArrayList<>();
        if (stringMap.size() > 0) {
            map.put(ServerConstant.REDIS_STRING, stringMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, List<String>> listMap = redisListService.getAllList();
        if (listMap.size() > 0) {
            map.put(ServerConstant.REDIS_LIST, listMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, Set<String>> setMap = redisSetService.getAllSet();
        if (setMap.size() > 0) {
            map.put(ServerConstant.REDIS_SET, setMap);
            list.add(map);
        }
        map = new HashMap<>();
        Map<String, Set<Tuple>> tupleMap = redisZSetService.getAllZSet();
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
        Map<String, Map<String, String>> hashMap = redisHashService.getAllHash();
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
                    redisStringService.saveAllString(stringMap);
                }
                if (map.containsKey(ServerConstant.REDIS_LIST)) {
                    Map listMap = (Map) map.get(ServerConstant.REDIS_LIST);
                    redisListService.saveAllList(listMap);
                }
                if (map.containsKey(ServerConstant.REDIS_SET)) {
                    Map setMap = (Map) map.get(ServerConstant.REDIS_SET);
                    redisSetService.saveAllSet(setMap);
                }
                if (map.containsKey(ServerConstant.REDIS_ZSET)) {
                    Map zSetMap = (Map) map.get(ServerConstant.REDIS_ZSET);
                    redisZSetService.saveAllZSet(zSetMap);
                }
                if (map.containsKey(ServerConstant.REDIS_HASH)) {
                    Map hashMap = (Map) map.get(ServerConstant.REDIS_HASH);
                    redisHashService.saveAllHash(hashMap);
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
                    redisStringService.saveAllStringSerialize(stringMap);
                }
                if (map.containsKey(ServerConstant.REDIS_LIST)) {
                    Map listMap = (Map) map.get(ServerConstant.REDIS_LIST);
                    redisListService.saveAllListSerialize(listMap);
                }
                if (map.containsKey(ServerConstant.REDIS_SET)) {
                    Map setMap = (Map) map.get(ServerConstant.REDIS_SET);
                    redisSetService.saveAllSetSerialize(setMap);
                }
                if (map.containsKey(ServerConstant.REDIS_ZSET)) {
                    Map zSetMap = (Map) map.get(ServerConstant.REDIS_ZSET);
                    redisZSetService.saveAllZSetSerialize(zSetMap);
                }
                if (map.containsKey(ServerConstant.REDIS_HASH)) {
                    Map hashMap = (Map) map.get(ServerConstant.REDIS_HASH);
                    redisHashService.saveAllHashSerialize(hashMap);
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
            redisService.flushAll();
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }
}
