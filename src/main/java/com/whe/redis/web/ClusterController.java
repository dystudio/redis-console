package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.whe.redis.service.ClusterService;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.ServerConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
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
public class ClusterController {
    private static final Logger log = LoggerFactory.getLogger(ClusterController.class);

    @Resource
    private ClusterService clusterService;

    private String contextPath = null;

    @RequestMapping("/index")
    public String index(String match, Model model, HttpServletRequest request, HttpServletResponse response) {
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        try {
            if (JedisFactory.getJedisCluster() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("[{");
                sb.append("text:").append("'").append("redisCluster").append("',");
                sb.append("icon:'").append(request.getContextPath()).append("/img/redis.png',").append("expanded:").append(true).append(",");
                sb.append("nodes:").append("[");
                sb.append("{text:").append("'").append("data").append("',").append("icon:'").append(contextPath).append("/img/db.png',").append("expanded:").append(true).append(",");
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
            }
            model.addAttribute("server", "/cluster");
        } catch (Exception e) {
            log.error("ClusterController index error:" + e.getMessage(), e);
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
                        return clusterService.setNxSerialize(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return clusterService.lPushSerialize(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return clusterService.sAddSerialize(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return clusterService.zAddSerialize(redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return clusterService.hSetNxSerialize(redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                }
            } else {
                switch (redis_type) {
                    case ServerConstant.REDIS_STRING:
                        return clusterService.setNx(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return clusterService.lPush(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return clusterService.sAdd(redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return clusterService.zAdd(redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return clusterService.hSetNx(redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                }
            }
        } catch (Exception e) {
            log.error("ClusterController save error:" + e.getMessage(), e);
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
                        clusterService.saveAllStringSerialize((Map) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (map.containsKey(ServerConstant.REDIS_LIST)) {
                        clusterService.saveAllListSerialize((Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (map.containsKey(ServerConstant.REDIS_SET)) {
                        clusterService.saveAllSetSerialize((Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (map.containsKey(ServerConstant.REDIS_ZSET)) {
                        clusterService.saveAllZSetSerialize((Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (map.containsKey(ServerConstant.REDIS_HASH)) {
                        clusterService.saveAllHashSerialize((Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("ClusterController serializeRecover error:" + e.getMessage(), e);
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
                        clusterService.saveAllString((Map<String, String>) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_LIST)) {
                        clusterService.saveAllList((Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_SET)) {
                        clusterService.saveAllSet((Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_ZSET)) {
                        clusterService.saveAllZSet((Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_HASH)) {
                        clusterService.saveAllHash((Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("ClusterController recover error:" + e.getMessage(), e);
            return e.getMessage();
        }
        return "1";
    }

    /**
     * 备份数据
     *
     * @param response response
     * @throws IOException IOException
     */
    @RequestMapping("/backup")
    public void backup(HttpServletResponse response) {
        try {
            LocalDate date = LocalDate.now();
            response.setContentType("text/plain; charset=utf-8");//设置MIME类型
            response.setHeader("Content-Disposition", "attachment; filename=" + date + "cluster.redis");
            response.getWriter().write(clusterService.backup());
        } catch (Exception e) {
            log.error("ClusterController backup error:" + e.getMessage(), e);
        }
    }

    @RequestMapping(value = {"/hSet"})
    @ResponseBody
    public String hSet(String key, String field, String val) {
        try {
            clusterService.hSet(key, field, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController hSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/hSet"})
    @ResponseBody
    public String hSetSerialize(String key, String field, String val) {
        try {
            clusterService.hSetSerialize(key, field, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController hSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateHash"})
    @ResponseBody
    public String updateHash(String key, String oldField, String newField, String val) {
        try {
            return clusterService.updateHash(key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            log.error("ClusterController updateHash error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateHash"})
    @ResponseBody
    public String updateHashSerialize(String key, String oldField, String newField, String val) {
        try {
            return clusterService.updateHashSerialize(key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            log.error("ClusterController updateHashSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delHash"})
    @ResponseBody
    public String delHash(String key, String field) {
        try {
            clusterService.delHash(key, field);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController delHash error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAllSerialize(String key) {
        try {
            return clusterService.hGetAll(key.getBytes(ServerConstant.CHARSET));
        } catch (Exception e) {
            log.error("ClusterController hGetAllSerialize error:" + e.getMessage(), e);
        }
        return null;
    }

    @RequestMapping(value = {"/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAll(String key) {
        return clusterService.hGetAll(key);
    }

    @RequestMapping(value = {"/updateZSet"})
    @ResponseBody
    public String updateZSet(String key, String oldVal, String newVal, double score) {
        try {
            clusterService.updateZSet(key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateZSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateZSet"})
    @ResponseBody
    public String updateZSetSerialize(String key, String oldVal, String newVal, double score) {
        try {
            clusterService.updateZSetSerialize(key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateZSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delZSet"})
    @ResponseBody
    public String delZSet(String key, String val) {
        try {
            clusterService.delZSet(key, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController delZSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getSerializeZSet(String key, int pageNo, HttpServletRequest request) {
        Page<Set<Tuple>> page = null;
        try {
            page = clusterService.findZSetPageByKey(key.getBytes(ServerConstant.CHARSET), pageNo);
            page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        } catch (UnsupportedEncodingException e) {
            log.error("ClusterController getSerializeZSet error:" + e.getMessage(), e);
        }
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
        Page<Set<Tuple>> page = clusterService.findZSetPageByKey(pageNo, key);
        page.pageViewAjax("/getZSet", "");
        return page;
    }

    @RequestMapping(value = {"/delSet"})
    @ResponseBody
    public String delSet(String key, String val) {
        try {
            clusterService.delSet(key, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController delSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateSet"})
    @ResponseBody
    public String updateSet(String key, String oldVal, String newVal) {
        try {
            clusterService.updateSet(key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateSet"})
    @ResponseBody
    public String updateSetSerialize(String key, String oldVal, String newVal) {
        try {
            clusterService.updateSetSerialize(key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getSet"})
    @ResponseBody
    public Set<String> getSerializeSet(String key) {
        try {
            return clusterService.getSet(key.getBytes(ServerConstant.CHARSET));
        } catch (Exception e) {
            log.error("ClusterController getSerializeSet error:" + e.getMessage(), e);
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
        return clusterService.getSet(key);
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
            long lLen = clusterService.lLen(key);
            if (listSize != lLen) {
                return "2";
            }
            clusterService.lRem(index, key);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController delList error:" + e.getMessage(), e);
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
            clusterService.lSet(index, key, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateList error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateList"})
    @ResponseBody
    public String updateListSerialize(int index, String key, String val) {
        try {
            clusterService.lSetSerialize(index, key, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateListSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getList"})
    @ResponseBody
    public Page<List<String>> getSerializeList(String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = null;
        try {
            page = clusterService.findListPageByKey(key.getBytes(ServerConstant.CHARSET), pageNo);
            page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        } catch (Exception e) {
            log.error("ClusterController getSerializeList error:" + e.getMessage(), e);
        }
        return page;
    }

    /**
     * ajax分页加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getList"})
    @ResponseBody
    public Page<List<String>> getList(String key, int pageNo) {
        Page<List<String>> page = clusterService.findListPageByKey(key, pageNo);
        page.pageViewAjax(contextPath + "/getList", "");
        return page;
    }


    @RequestMapping("/updateString")
    @ResponseBody
    public String updateString(String key, String val) {
        try {
            clusterService.set(key, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateString error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/serialize/updateString")
    @ResponseBody
    public String updateStringSerialize(String key, String val) {
        try {
            clusterService.setSerialize(key, val);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController updateStringSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/getString"})
    @ResponseBody
    public String getSerializeString(String key) {
        try {
            return clusterService.get(key.getBytes(ServerConstant.CHARSET));
        } catch (UnsupportedEncodingException e) {
            log.error("ClusterController getSerializeString error:" + e.getMessage(), e);
        }
        return "";
    }

    @RequestMapping("/getString")
    @ResponseBody
    public String getString(String key) {
        return clusterService.get(key);
    }

    @RequestMapping("/delKey")
    @ResponseBody
    public String delKey(String key) {
        try {
            clusterService.del(key);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController delKey error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/renameNx")
    @ResponseBody
    public String renameNx(String oldKey, String newKey) {
        try {
            return clusterService.renameNx(oldKey, newKey) == 0 ? "2" : "1";
        } catch (Exception e) {
            log.error("ClusterController renameNx error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/ttl")
    @ResponseBody
    public long ttl(String key) {
        return clusterService.ttl(key);
    }

    @RequestMapping("/setExpire")
    @ResponseBody
    public String setExpire(String key, int seconds) {
        try {
            clusterService.expire(key, seconds);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController setExpire error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/persist")
    @ResponseBody
    public String persist(String key) {
        try {
            clusterService.persist(key);
            return "1";
        } catch (Exception e) {
            log.error("ClusterController persist error:" + e.getMessage(), e);
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
        try {
            return page(pageNo, match, request, response);
        } catch (Exception e) {
            log.error("ClusterController nextPage error:" + e.getMessage(), e);
            return "";
        }
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
            clusterService.flushAll();
        } catch (Exception e) {
            log.error("ClusterController flushAll error:" + e.getMessage(), e);
            return e.getMessage();
        }
        return "1";
    }

    private String page(Integer pageNo, String match, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String data = null;
        if (cookies != null && cookies.length > 0) {
            Optional<Cookie> optional = Stream
                    .of(cookies)
                    .filter(c -> c.getName().equals(ServerConstant.CLUSTER_PAGE))
                    .findAny();

            if (optional.isPresent()) {
                Cookie cookie = optional.get();
                try {
                    String value = URLDecoder.decode(cookie.getValue(), ServerConstant.CHARSET);

                    Map<Integer, Map<String, String>> map = JSON.parseObject(value, Map.class);
                    Map<String, String> nodeCursor = map.get(pageNo);
                    data = dataTree(pageNo, match, nodeCursor, map);

                    cookie.setValue(URLEncoder.encode(JSON.toJSONString(map), ServerConstant.CHARSET));
                } catch (Exception e) {
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
        Map<String, ScanResult<String>> nodeScan = clusterService.scan(nodeCursor, match);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Map<String, String> nextNodeCursor = nodeScan.entrySet().stream().filter(entry -> {
            Map<String, String> typeMap = clusterService.getType(entry.getValue().getResult());
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
