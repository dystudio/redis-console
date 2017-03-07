package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.whe.redis.service.RedisClusterService;
import com.whe.redis.util.Page;
import com.whe.redis.util.ServerConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wang hongen on 2017/2/24.
 * RedisClusterController
 */
@Controller
@RequestMapping("/cluster")
public class RedisClusterController {
    @Autowired
    private RedisClusterService redisClusterService;

    private String contextPath = null;

    @RequestMapping("/index")
    public String index(String match, Model model, HttpServletRequest request, HttpServletResponse response) {
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[{");
            sb.append("text:").append("'").append("redisCluster").append("',");
            sb.append("icon:").append(request.getContextPath()).append("'/img/redis.png',").append("expanded:").append(true).append(",");
            sb.append("nodes:").append("[");
            sb.append("{text:").append("'").append("data").append("',").append("icon:").append(contextPath).append("'/img/db.png',").append("expanded:").append(true).append(",");
            sb.append("nodes:");
            Map<Integer, Map<String, String>> map = new HashMap<>();
            Map<String, String> nodeCursor = new HashMap<>();
            map.put(1, nodeCursor);
            sb.append(dataTree(1, match, nodeCursor, map));
            sb.append("}]}]");
            model.addAttribute("tree", sb.toString());
            String jsonString = JSON.toJSONString(map);
            String encode = null;
            try {
                encode = URLEncoder.encode(jsonString, ServerConstant.CHARSET);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Cookie cookie = new Cookie(ServerConstant.CLUSTER_PAGE, encode);
            cookie.setPath("/");
            cookie.setMaxAge(-1);
            response.addCookie(cookie);
            model.addAttribute("match", match);
            model.addAttribute("server", "/cluster");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(String redis_key, String redis_type, Double redis_score, String redis_field, String redis_value, String redis_serializable) {
        try {
            if ("1".equals(redis_serializable)) {
                switch (redis_type) {
                    case ServerConstant.REDIS_STRING:
                        return redisClusterService.setNxSerialize(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return redisClusterService.lPushSerialize(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return redisClusterService.sAddSerialize(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return redisClusterService.zAddSerialize(redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return redisClusterService.hSetNxSerialize(redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                }
            } else {
                switch (redis_type) {
                    case ServerConstant.REDIS_STRING:
                        return redisClusterService.setNx(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return redisClusterService.lPush(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return redisClusterService.sAdd(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return redisClusterService.zAdd(redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return redisClusterService.hSetNx(redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "0";
    }

    /**
     * 序列化恢复数据
     *
     * @return string
     */
    @RequestMapping("/serializeRecover")
    @ResponseBody
    public String serializeRecover(MultipartFile file) {
        try {
            String data = new String(file.getBytes(), ServerConstant.CHARSET);
            Object obj = JSON.parse(data);
            if (obj instanceof Map) {
                Map<String, Map> map = (Map) obj;
                boolean isCluster = false;
                for (Map.Entry<String, Map> entry : map.entrySet()) {
                    Map nowMap;
                    try {
                        Integer.parseInt(entry.getKey());
                        nowMap = entry.getValue();
                    } catch (Exception e) {
                        isCluster = true;
                        nowMap = map;
                    }
                    if (map.containsKey(ServerConstant.REDIS_STRING)) {
                        redisClusterService.saveAllStringSerialize((Map) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (map.containsKey(ServerConstant.REDIS_LIST)) {
                        redisClusterService.saveAllListSerialize((Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (map.containsKey(ServerConstant.REDIS_SET)) {
                        redisClusterService.saveAllSetSerialize((Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (map.containsKey(ServerConstant.REDIS_ZSET)) {
                        redisClusterService.saveAllZSetSerialize((Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (map.containsKey(ServerConstant.REDIS_HASH)) {
                        redisClusterService.saveAllHashSerialize((Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "1";
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
            String data = new String(file.getBytes(), ServerConstant.CHARSET);
            Object obj = JSON.parse(data);
            if (obj instanceof Map) {
                Map<String, Map> map = (Map) obj;
                boolean isCluster = false;
                for (Map.Entry<String, Map> entry : map.entrySet()) {
                    Map nowMap;
                    try {
                        Integer.parseInt(entry.getKey());
                        nowMap = entry.getValue();
                    } catch (Exception e) {
                        isCluster = true;
                        nowMap = map;
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_STRING)) {
                        redisClusterService.saveAllString((Map<String, String>) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_LIST)) {
                        redisClusterService.saveAllList((Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_SET)) {
                        redisClusterService.saveAllSet((Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_ZSET)) {
                        redisClusterService.saveAllZSet((Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_HASH)) {
                        redisClusterService.saveAllHash((Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "1";
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
        LocalDate date = LocalDate.now();
        response.setContentType(request.getServletContext().getMimeType(date + "cluster.redis"));//设置MIME类型
        response.setHeader("Content-Disposition", "attachment; filename=" + date + "cluster.redis");
        response.getWriter().write(redisClusterService.getAll());
    }

    @RequestMapping(value = {"/hSet"})
    @ResponseBody
    public String hSet(String key, String field, String val) {
        try {
            redisClusterService.hSet(key, field, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateHash"})
    @ResponseBody
    public String updateHash(String key, String oldField, String newField, String val) {
        try {
            return redisClusterService.updateHash(key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delHash"})
    @ResponseBody
    public String delHash(String key, String field) {
        try {
            redisClusterService.delHash(key, field);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAllSerialize(String key) {
        try {
            return redisClusterService.hGetAll(key.getBytes(ServerConstant.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = {"/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAll(String key) {
        return redisClusterService.hGetAll(key);
    }

    @RequestMapping(value = {"/updateZSet"})
    @ResponseBody
    public String updateZSet(String key, String oldVal, String newVal, double score) {
        try {
            redisClusterService.updateZSet(key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delZSet"})
    @ResponseBody
    public String delZSet(String key, String val) {
        try {
            redisClusterService.delZSet(key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getSerializeZSet(String key, int pageNo, HttpServletRequest request) {
        Page<Set<Tuple>> page = null;
        try {
            page = redisClusterService.findZSetPageByKey(key.getBytes(ServerConstant.CHARSET), pageNo);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert page != null;
        page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        return page;
    }

    /**
     * ajax加载zSet类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getZSet(int pageNo, String key) {
        Page<Set<Tuple>> page = redisClusterService.findZSetPageByKey(pageNo, key);
        page.pageViewAjax("/getZSet", "");
        return page;
    }

    @RequestMapping(value = {"/delSet"})
    @ResponseBody
    public String delSet(String key, String val) {
        try {
            redisClusterService.delSet(key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateSet"})
    @ResponseBody
    public String updateSet(String key, String oldVal, String newVal) {
        try {
            redisClusterService.updateSet(key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getSet"})
    @ResponseBody
    public Set<String> getSerializeSet(String key) {
        try {
            return redisClusterService.getSet(key.getBytes(ServerConstant.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ajax加载所有set类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getSet"})
    @ResponseBody
    public Set<String> getSet(String key) {
        return redisClusterService.getSet(key);
    }

    /**
     * list根据索引删除
     *
     * @param listSize listSize
     * @param index    index
     * @param key      key
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/delList"})
    @ResponseBody
    public String delList(int listSize, int index, String key) {
        try {
            long lLen = redisClusterService.lLen(key);
            if (listSize != lLen) {
                return "2";
            }
            redisClusterService.lRem(index, key);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * list根据索引更新value
     *
     * @param index index
     * @param key   key
     * @param val   val
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/updateList"})
    @ResponseBody
    public String updateList(int index, String key, String val) {
        try {
            redisClusterService.lSet(index, key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getList"})
    @ResponseBody
    public Page<List<String>> getSerializeList(String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = null;
        try {
            page = redisClusterService.findListPageByKey(key.getBytes(ServerConstant.CHARSET), pageNo);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert page != null;
        page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        return page;
    }

    /**
     * ajax分页加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getList"})
    @ResponseBody
    public Page<List<String>> getList(String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = redisClusterService.findListPageByKey(key, pageNo);
        page.pageViewAjax(contextPath + "/getList", "");
        return page;
    }


    @RequestMapping("/updateString")
    @ResponseBody
    public String updateString(String key, String val) {
        try {
            redisClusterService.set(key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getString"})
    @ResponseBody
    public String getSerializeString(String key) {
        try {
            return redisClusterService.get(key.getBytes(ServerConstant.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping("/getString")
    @ResponseBody
    public String getString(String key) {
        return redisClusterService.get(key);
    }

    @RequestMapping("/delKey")
    @ResponseBody
    public String delKey(String key) {
        try {
            return redisClusterService.del(key) == 1 ? "1" : "0";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping("/renameNx")
    @ResponseBody
    public String renameNx(String oldKey, String newKey) {
        try {
            return redisClusterService.renameNx(oldKey, newKey) == 0 ? "2" : "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping("/ttl")
    @ResponseBody
    public long ttl(String key) {
        return redisClusterService.ttl(key);
    }

    @RequestMapping("/setExpire")
    @ResponseBody
    public String setExpire(String key, int seconds) {
        try {
            redisClusterService.expire(key, seconds);
            return "1";
        } catch (Exception e) {
            e.getMessage();
            return e.getMessage();
        }
    }

    @RequestMapping("/persist")
    @ResponseBody
    public String persist(String key) {
        try {
            redisClusterService.persist(key);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping("/upPage")
    @ResponseBody
    public String upPage(Integer pageNo, String match, HttpServletRequest request, HttpServletResponse response) {
        return page(pageNo, match, request, response);
    }

    @RequestMapping("/nextPage")
    @ResponseBody
    public String nextPage(Integer pageNo, String match, HttpServletRequest request, HttpServletResponse response) {
        return page(pageNo, match, request, response);
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

    private String page(Integer pageNo, String match, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String data = null;
        if (cookies != null && cookies.length > 0) {
            Optional<Cookie> optional = Stream.of(cookies).filter(c -> c.getName().equals(ServerConstant.CLUSTER_PAGE)).findAny();
            if (optional.isPresent()) {
                Cookie cookie = optional.get();
                String value = null;
                try {
                    value = URLDecoder.decode(cookie.getValue(), ServerConstant.CHARSET);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Map<Integer, Map<String, String>> map = JSON.parseObject(value, Map.class);
                Map<String, String> nodeCursor = map.get(pageNo);
                data = dataTree(pageNo, match, nodeCursor, map);
                try {
                    cookie.setValue(URLEncoder.encode(JSON.toJSONString(map), ServerConstant.CHARSET));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                response.addCookie(cookie);
            }
        }
        return data;
    }

    private String dataTree(int pageNo, String match, Map<String, String> nodeCursor, Map<Integer, Map<String, String>> map) {
        if (StringUtils.isBlank(match)) {
            match = ServerConstant.DEFAULT_MATCH;
        } else {
            match = "*" + match + "*";
        }
        Map<String, ScanResult<String>> nodeScan = redisClusterService.scan(nodeCursor, match);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Map<String, String> nextNodeCursor = nodeScan.entrySet().stream().filter(entry -> {
            Map<String, String> typeMap = redisClusterService.getType(entry.getValue().getResult());
            typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(contextPath).append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
            return !ServerConstant.DEFAULT_CURSOR.equalsIgnoreCase(entry.getValue().getStringCursor());
        }).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getStringCursor()));

        sb.append("{page:").append("'<ul class=\"pagination\" style=\"margin:0px\"> <li ");
        if (pageNo == 1) {
            sb.append(" class=\"disabled\"");
        }
        sb.append("><a  href=\"javascript:void(0);\" onclick=\"clusterUpPage(").append(pageNo - 1).append(",").append("event)").append(" \">上一页</a></li>").append("<li");
        if (nextNodeCursor.size() > 0) {
            map.put(pageNo + 1, nextNodeCursor);
        } else {
            sb.append(" class=\"disabled\"");
        }
        sb.append("> <a  href=\"javascript:void(0);\" onclick=\"clusterNextPage(").append(pageNo + 1).append(",").append("event)").append(" \">下一页</a></li></ul>'},");
        sb.deleteCharAt(sb.length() - 1).append("]");
        return sb.toString();
    }
}
