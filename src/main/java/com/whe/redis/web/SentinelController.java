package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.whe.redis.service.SentinelService;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.ServerConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.util.stream.Stream;

/**
 * Created by wang hongen on 2017/3/9.
 * SentinelController
 */
@Controller
@RequestMapping("/sentinel")
public class SentinelController {
    private static final Logger log = LoggerFactory.getLogger(StandaloneController.class);

    @Resource
    private SentinelService sentinelService;


    /**
     * 入口 首页
     *
     * @param model model
     * @return index
     */
    @RequestMapping(value = {"/index"})
    public String index(Model model, @RequestParam(defaultValue = "0") String cursor, String match, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (JedisFactory.getJedisSentinelPool() != null) {
                String treeJson = treeJson(cursor, match, request, response);
                Integer size = sentinelService.getDataBasesSize();
                model.addAttribute("dataSize", size);
                model.addAttribute("tree", treeJson);
                model.addAttribute("match", match);
            }
            model.addAttribute("server", "/sentinel");
        } catch (Exception e) {
            log.error("SentinelController index error:" + e.getMessage(), e);
        }
        return "index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(String redis_key, Integer redis_data_size, String redis_type, Double redis_score, String redis_field, String redis_value, String redis_serializable) {
        try {
            if ("1".equals(redis_serializable)) {
                switch (redis_type) {
                    case ServerConstant.REDIS_STRING:
                        return sentinelService.setNxSerialize(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return sentinelService.lPushSerialize(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return sentinelService.sAddSerialize(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return sentinelService.zAddSerialize(redis_data_size, redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return sentinelService.hSetNxSerialize(redis_data_size, redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                }
            } else {
                switch (redis_type) {
                    case ServerConstant.REDIS_STRING:
                        return sentinelService.setNx(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return sentinelService.lPush(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return sentinelService.sAdd(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return sentinelService.zAdd(redis_data_size, redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return sentinelService.hSetNx(redis_data_size, redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                }
            }
        } catch (Exception e) {
            log.error("SentinelController save error:" + e.getMessage(), e);
            return e.getMessage();
        }
        return "0";
    }

    /**
     * ajax加载string类型数据
     *
     * @return String
     */
    @RequestMapping(value = {"/getString"})
    @ResponseBody
    public String getString(Integer db, String key) {
        return sentinelService.getString(db, key);
    }

    @RequestMapping(value = {"/serialize/getString"})
    @ResponseBody
    public String getSerializeString(Integer db, String key) {
        try {
            return sentinelService.getStringSerialize(db, key);
        } catch (Exception e) {
            log.error("SentinelController getSerializeString error:" + e.getMessage(), e);
        }
        return "";
    }

    /**
     * ajax分页加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getList"})
    @ResponseBody
    public Page<List<String>> getList(int db, String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = sentinelService.findListPageByKey(db, key, pageNo);
        page.pageViewAjax(request.getContextPath() + "/getList", "");
        return page;
    }

    @RequestMapping(value = {"/serialize/getList"})
    @ResponseBody
    public Page<List<String>> getSerializeList(int db, String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = null;
        try {
            page = sentinelService.findListPageByKeySerialize(db, key, pageNo);
            page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        } catch (UnsupportedEncodingException e) {
            log.error("SentinelController getSerializeList error:" + e.getMessage(), e);
        }
        return page;
    }

    /**
     * ajax加载set类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getSet"})
    @ResponseBody
    public Set<String> getSet(int db, String key) {
        return sentinelService.getSet(db, key);
    }

    @RequestMapping(value = {"/serialize/getSet"})
    @ResponseBody
    public Set<String> getSerializeSet(int db, String key) {
        return sentinelService.getSetSerialize(db, key);
    }

    /**
     * ajax加载zSet类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getZSet(int db, int pageNo, String key) {
        Page<Set<Tuple>> page = sentinelService.findZSetPageByKey(db, pageNo, key);
        page.pageViewAjax("/getZSet", "");
        return page;
    }

    @RequestMapping(value = {"/serialize/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getSerializeZSet(int db, String key, int pageNo, HttpServletRequest request) {
        Page<Set<Tuple>> page = null;
        try {
            page = sentinelService.findZSetPageByKeySerialize(db, key, pageNo);
            page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        } catch (Exception e) {
            log.error("SentinelController getSerializeZSet error:" + e.getMessage(), e);
        }
        return page;
    }

    @RequestMapping(value = {"/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAll(int db, String key) {
        return sentinelService.hGetAll(db, key);
    }

    @RequestMapping(value = {"/serialize/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAllSerialize(int db, String key) {
        try {
            return sentinelService.hGetAllSerialize(db, key);
        } catch (Exception e) {
            log.error("SentinelController hGetAllSerialize error:" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * key重命名
     *
     * @param db     db
     * @param oldKey 旧key
     * @param newKey 新key
     * @return 2:key已存在;1:成功;0:失败
     */
    @RequestMapping(value = {"/renameNx"})
    @ResponseBody
    public String renameNx(int db, String oldKey, String newKey) {
        try {
            return sentinelService.renameNx(db, oldKey, newKey) == 0 ? "2" : "1";
        } catch (Exception e) {
            log.error("SentinelController renameNx error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * 获得生存时间
     *
     * @param db  db
     * @param key key
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/ttl"})
    @ResponseBody
    public long ttl(int db, String key) {
        try {
            return sentinelService.ttl(db, key);
        } catch (Exception e) {
            log.error("SentinelController ttl error:" + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 更新生存时间
     *
     * @param db      db
     * @param key     key
     * @param seconds 秒
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/setExpire"})
    @ResponseBody
    public String setExpire(int db, String key, int seconds) {
        try {
            sentinelService.setExpire(db, key, seconds);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController setExpire error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * 移除 key 的生存时间
     *
     * @param db  db
     * @param key key
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/persist"})
    @ResponseBody
    public String persist(int db, String key) {
        try {
            sentinelService.persist(db, key);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController persist error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * 删除key
     *
     * @param db  db
     * @param key key
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/delKey"})
    @ResponseBody
    public String delKey(int db, String key) {
        try {
            sentinelService.delKey(db, key);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController delKey error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * string更新值
     *
     * @param db  db
     * @param key key
     * @param val newValue
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/updateString"})
    @ResponseBody
    public String updateString(int db, String key, String val) {
        try {
            sentinelService.set(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateString error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/serialize/updateString")
    @ResponseBody
    public String updateStringSerialize(int db, String key, String val) {
        try {
            sentinelService.setSerialize(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateStringSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * list根据索引更新value
     *
     * @param db    db
     * @param index index
     * @param key   key
     * @param val   val
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/updateList"})
    @ResponseBody
    public String updateList(int db, int index, String key, String val) {
        try {
            sentinelService.lSet(db, index, key, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateList error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateList"})
    @ResponseBody
    public String updateListSerialize(int db, int index, String key, String val) {
        try {
            sentinelService.lSetSerialize(db, index, key, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateListSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * list根据索引删除
     *
     * @param db       db
     * @param listSize listSize
     * @param index    index
     * @param key      key
     * @return 1:成功;0:失败
     */
    @RequestMapping(value = {"/delList"})
    @ResponseBody
    public String delList(int db, int listSize, int index, String key) {
        try {
            long lLen = sentinelService.lLen(db, key);
            if (listSize != lLen) {
                return "2";
            }
            sentinelService.lRem(db, index, key);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController delList error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = {"/updateSet"})
    @ResponseBody
    public String updateSet(int db, String key, String oldVal, String newVal) {
        try {
            sentinelService.updateSet(db, key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateSet"})
    @ResponseBody
    public String updateSetSerialize(int db, String key, String oldVal, String newVal) {
        try {
            sentinelService.updateSetSerialize(db, key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delSet"})
    @ResponseBody
    public String delSet(int db, String key, String val) {
        try {
            sentinelService.delSet(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController delSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateZSet"})
    @ResponseBody
    public String updateZSet(int db, String key, String oldVal, String newVal, double score) {
        try {
            sentinelService.updateZSet(db, key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateZSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateZSet"})
    @ResponseBody
    public String updateZSetSerialize(int db, String key, String oldVal, String newVal, double score) {
        try {
            sentinelService.updateZSetSerialize(db, key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController updateZSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delZSet"})
    @ResponseBody
    public String delZSet(int db, String key, String val) {
        try {
            sentinelService.delZSet(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController delZSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/hSet"})
    @ResponseBody
    public String hSet(int db, String key, String field, String val) {
        try {
            sentinelService.hSet(db, key, field, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController hSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/hSet"})
    @ResponseBody
    public String hSetSerialize(int db, String key, String field, String val) {
        try {
            sentinelService.hSetSerialize(db, key, field, val);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController hSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateHash"})
    @ResponseBody
    public String updateHash(int db, String key, String oldField, String newField, String val) {
        try {
            return sentinelService.updateHash(db, key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            log.error("SentinelController updateHash error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateHash"})
    @ResponseBody
    public String updateHashSerialize(int db, String key, String oldField, String newField, String val) {
        try {
            return sentinelService.updateHashSerialize(db, key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            log.error("SentinelController updateHashSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delHash"})
    @ResponseBody
    public String delHash(int db, String key, String field) {
        try {
            sentinelService.delHash(db, key, field);
            return "1";
        } catch (Exception e) {
            log.error("SentinelController delHash error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    /**
     * 备份数据
     *
     * @param response response
     * @throws IOException IOException
     */
    @RequestMapping("/backup")
    public void backup(HttpServletResponse response){
        try {
            String str = sentinelService.backup();
            LocalDate data = LocalDate.now();
            response.setContentType("text/plain; charset=utf-8");//设置MIME类型
            response.setHeader("Content-Disposition", "attachment; filename=" + data + "standalone.redis");
            response.getWriter().write(str);
        }catch (Exception e){
            log.error("SentinelController backup error:" + e.getMessage(), e);
        }
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
                    int db;
                    try {
                        db = Integer.parseInt(entry.getKey());
                        nowMap = entry.getValue();
                    } catch (Exception e) {
                        nowMap = map;
                        isCluster = true;
                        db = 0;
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_STRING)) {
                        sentinelService.saveAllString(db, (Map<String, String>) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_LIST)) {
                        sentinelService.saveAllList(db, (Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_SET)) {
                        sentinelService.saveAllSet(db, (Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_ZSET)) {
                        sentinelService.saveAllZSet(db, (Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_HASH)) {
                        sentinelService.saveAllHash(db, (Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("SentinelController recover error:" + e.getMessage(), e);
            return e.getMessage();
        }
        return "1";
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
                    int db;
                    try {
                        db = Integer.parseInt(entry.getKey());
                        nowMap = entry.getValue();
                    } catch (Exception e) {
                        isCluster = true;
                        db = 0;
                        nowMap = map;
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_STRING)) {
                        sentinelService.saveAllStringSerialize(db, (Map<String, String>) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_LIST)) {
                        sentinelService.saveAllListSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_SET)) {
                        sentinelService.saveAllSetSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_ZSET)) {
                        sentinelService.saveAllZSetSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_HASH)) {
                        sentinelService.saveAllHashSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("SentinelController serializeRecover error:" + e.getMessage(), e);
            return e.getMessage();
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
            sentinelService.flushAll();
            return "1";
        } catch (Exception e) {
            log.error("SentinelController flushAll error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/upPage")
    @ResponseBody
    public String upPage(Integer db, String cursor, String match, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String upCursor = getUpCursorByCookie(cookies, db, cursor);
        return dbJson(db, cursor, upCursor, match, request);
    }

    @RequestMapping("/nextPage")
    @ResponseBody
    public String nextPage(Integer db, String cursor, String match, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            Optional<Cookie> cookie = Stream.of(cookies).filter(c -> c.getName().equals(ServerConstant.REDIS_CURSOR)).findAny();
            cookie.ifPresent(c -> {
                String value = null;
                try {
                    value = URLDecoder.decode(c.getValue(), ServerConstant.CHARSET);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Map map = JSON.parseObject(value, Map.class);
                List<String> list = (List<String>) map.get(db);
                list.add(cursor);
                try {
                    c.setValue(URLEncoder.encode(JSON.toJSONString(map), ServerConstant.CHARSET));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                response.addCookie(c);
            });
        }
        String upCursor = getUpCursorByCookie(cookies, db, cursor);
        return dbJson(db, cursor, upCursor, match, request);
    }

    private String getUpCursorByCookie(Cookie[] cookies, Integer db, String cursor) {
        if (ServerConstant.DEFAULT_CURSOR.equals(cursor)) {
            return ServerConstant.DEFAULT_CURSOR;
        }
        return Stream.of(cookies).filter(c -> c.getName().equals(ServerConstant.REDIS_CURSOR)).findAny().map(c -> {
            String value = null;
            try {
                value = URLDecoder.decode(c.getValue(), ServerConstant.CHARSET);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Map map = JSON.parseObject(value, Map.class);
            List<String> list = (List<String>) map.get(db);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(cursor)) {
                    return list.get(i - 1);
                }
            }
            return ServerConstant.DEFAULT_CURSOR;
        }).orElse(ServerConstant.DEFAULT_CURSOR);
    }

    private String treeJson(String cursor, String match, HttpServletRequest request, HttpServletResponse response) {
        Map<Integer, Long> dataBases = sentinelService.getDataBases();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("text:").append("'").append(JedisFactory.getSentinel()).append("',");
        sb.append("icon:'").append(request.getContextPath()).append("/img/redis.png',").append("expanded:").append(true).append(",");
        sb.append("nodes:").append("[");
        Map<Integer, List<String>> map = new HashMap<>();
        dataBases.entrySet().forEach(entry -> {
            sb.append("{text:").append("'").append(ServerConstant.DB).append(entry.getKey()).append("',").append("icon:'").append(request.getContextPath()).append("/img/db.png',").append("tags:").append("['").append(entry.getValue()).append("']");
            Long dbSize = entry.getValue();
            if (dbSize > 0) {
                ScanResult<String> scanResult = sentinelService.getKeysByDb(entry.getKey(), cursor, match);
                sb.append(",").append("expanded:").append(true).append(",");
                sb.append("nodes:").append("[");
                Map<String, String> typeMap = sentinelService.getType(entry.getKey(), scanResult.getResult());
                typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(request.getContextPath()).append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
                if (scanResult.getResult().size() >= ServerConstant.PAGE_NUM) {
                    List<String> list = new ArrayList<>();
                    list.add("0");
                    map.put(entry.getKey(), list);
                    String stringCursor = scanResult.getStringCursor();
                    sb.append("{page:").append("'<ul class=\"pagination\" style=\"margin:0px\"> <li class=\"disabled\" ><a  href=\"javascript:void(0);\" onclick=\"upPage(").append(entry.getKey()).append(",").append(ServerConstant.DEFAULT_CURSOR).append(",event)").append(" \">上一页</a></li><li ");
                    if ("0".equals(stringCursor)) {
                        sb.append(" class=\"disabled\"");
                    }
                    sb.append("> <a  href=\"javascript:void(0);\" onclick=\"nextPage(").append(entry.getKey()).append(",").append(stringCursor).append(",event)").append(" \">下一页</a></li></ul>'}");
                }
                sb.append("]");
            }
            sb.append("},");
        });
        sb.deleteCharAt(sb.length() - 1).append("]}]");
        String jsonString = JSON.toJSONString(map);
        String encode = null;
        try {
            encode = URLEncoder.encode(jsonString, ServerConstant.CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Cookie cookie = new Cookie(ServerConstant.REDIS_CURSOR, encode);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
        return sb.toString();
    }

    private String dbJson(Integer db, String cursor, String upCursor, String match, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        ScanResult<String> scanResult = sentinelService.getKeysByDb(db, cursor, match);
        sb.append("[");
        Map<String, String> typeMap = sentinelService.getType(db, scanResult.getResult());
        typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(request.getContextPath()).append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
        String stringCursor = scanResult.getStringCursor();
        sb.append("{page:").append("'<ul class=\"pagination\" style=\"margin:0px\"> <li ");
        if (cursor == null || "0".equals(cursor)) {
            sb.append(" class=\"disabled\"");
        }
        sb.append("><a  href=\"javascript:void(0);\" onclick=\"upPage(").append(db).append(",").append(upCursor).append(",event)").append(" \">上一页</a></li><li ");
        if ("0".equals(stringCursor)) {
            sb.append(" class=\"disabled\"");
        }
        sb.append("> <a  href=\"javascript:void(0);\" onclick=\"nextPage(").append(db).append(",").append(stringCursor).append(",event)").append(" \">下一页</a></li></ul>'}");
        sb.append("]");
        return sb.toString();
    }
}
