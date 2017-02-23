package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.whe.redis.service.*;
import com.whe.redis.util.JedisFactory;
import com.whe.redis.util.Page;
import com.whe.redis.util.ServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

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
    public void keys() {
        redisService.keys();
    }

    /**
     * 入口 首页
     *
     * @param model model
     * @return index
     */
    @RequestMapping(value = {"/"})
    public String index(Model model, @RequestParam(defaultValue = "0") String cursor, String pattern, HttpServletRequest request, HttpServletResponse response) {

        String treeJson = treeJson(cursor, request, response);
        model.addAttribute("tree", treeJson);
        Set<String> type = new HashSet<>();
        type.add("string");
        type.add("list");
        type.add("set");
        type.add("zSet");
        type.add("hash");
        model.addAttribute("type", type);

        return "index";
    }

    /**
     * ajax加载string类型数据
     *
     * @return String
     */
    @RequestMapping(value = {"/getString"})
    @ResponseBody
    public String getString(Integer db, String key) {
        return redisStringService.getString(db, key);
    }

    /**
     * ajax加载所有string类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/string"})
    @ResponseBody
    public Map<String, String> string(String pattern) {
        return redisStringService.getAllString(pattern);
    }

    /**
     * ajax分页加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getList"})
    @ResponseBody
    public Page<List<String>> getList(int db, String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = redisListService.findListPageByKey(db, key, pageNo);
        page.pageViewAjax(request.getContextPath() + "/getList", "");
        return page;
    }

    /**
     * ajax加载所有list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/list"})
    @ResponseBody
    public Map<String, List<String>> list() {
        return redisListService.getAllList();
    }

    /**
     * ajax加载所有set类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getSet"})
    @ResponseBody
    public Set<String> getSet(int db, String key) {
        return redisSetService.getSet(db, key);
    }

    /**
     * ajax加载所有set类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getZSet(int db, int pageNo, String key) {
        Page<Set<Tuple>> page = redisZSetService.findZSetPageByKey(db, pageNo, key);
        page.pageViewAjax("/getZSet", "");
        return page;
    }

    /**
     * ajax加载所有zSet类型数据
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

    @RequestMapping(value = {"/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAll(int db, String key) {
        return redisHashService.hGetAll(db, key);
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
            return redisService.renameNx(db, oldKey, newKey) == 0 ? "2" : "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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
            return redisService.ttl(db, key);
        } catch (Exception e) {
            e.printStackTrace();
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
            redisService.setExpire(db, key, seconds);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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
            redisService.delKey(db, key);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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
            redisStringService.updateVal(db, key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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
            redisListService.lSet(db, index, key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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
            long lLen = redisListService.lLen(db, key);
            if (listSize != lLen) {
                return "2";
            }
            redisListService.lRem(db, index, key);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }


    @RequestMapping(value = {"/updateSet"})
    @ResponseBody
    public String updateSet(int db, String key, String oldVal, String newVal) {
        try {
            redisSetService.updateSet(db, key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/delSet"})
    @ResponseBody
    public String delSet(int db, String key, String val) {
        try {
            redisSetService.delSet(db, key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/updateZSet"})
    @ResponseBody
    public String updateZSet(int db, String key, String oldVal, String newVal, double score) {
        try {
            redisZSetService.updateZSet(db, key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/delZSet"})
    @ResponseBody
    public String delZSet(int db, String key, String val) {
        try {
            redisZSetService.delZSet(db, key, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/hSet"})
    @ResponseBody
    public String hSet(int db, String key, String field, String val) {
        try {
            redisHashService.hSet(db, key, field, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/updateHash"})
    @ResponseBody
    public String updateHash(int db, String key, String oldField, String newField, String val) {
        try {
            redisHashService.updateHash(db, key, oldField, newField, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/delHash"})
    @ResponseBody
    public String delHash(int db, String key, String field) {
        try {
            redisHashService.delHash(db, key, field);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
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

    @RequestMapping("/upPage")
    @ResponseBody
    public String upPage(Integer db, String cursor, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String upCursor = getUpCursorByCookie(cookies, db, cursor);
        return dbJson(db, cursor, upCursor, request);
    }

    @RequestMapping("/nextPage")
    @ResponseBody
    public String nextPage(Integer db, String cursor, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            Optional<Cookie> cookie = Stream.of(cookies).filter(c -> c.getName().equals(ServerConstant.REDIS_CURSOR)).findAny();
            cookie.ifPresent(c -> {
                String value = null;
                try {
                    value = URLDecoder.decode(c.getValue(), ServerConstant.ENCODING);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Map map = JSON.parseObject(value, Map.class);
                List<String> list = (List<String>) map.get(db);
                list.add(cursor);
                try {
                    c.setValue(URLEncoder.encode(JSON.toJSONString(map), ServerConstant.ENCODING));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                response.addCookie(c);
            });
        }
        String upCursor = getUpCursorByCookie(cookies, db, cursor);
        return dbJson(db, cursor, upCursor, request);
    }

    private String getUpCursorByCookie(Cookie[] cookies, Integer db, String cursor) {
        if (ServerConstant.DEFAULT_CURSOR.equals(cursor)) {
            return ServerConstant.DEFAULT_CURSOR;
        }
        return Stream.of(cookies).filter(c -> c.getName().equals(ServerConstant.REDIS_CURSOR)).findAny().map(c -> {
            String value = null;
            try {
                value = URLDecoder.decode(c.getValue(), ServerConstant.ENCODING);
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

    private String treeJson(String cursor, HttpServletRequest request, HttpServletResponse response) {
        Map<Integer, Long> dataBases = redisService.getDataBases();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("text:").append("'").append(JedisFactory.getStandAlone()).append("',");
        sb.append("icon:").append(request.getContextPath()).append("'/img/redis.png',").append("expanded:").append(true).append(",");
        sb.append("nodes:").append("[");
        Map<Integer, List<String>> map = new HashMap<>();
        dataBases.entrySet().forEach(entry -> {
            sb.append("{text:").append("'").append("DB-").append(entry.getKey()).append("',").append("icon:").append(request.getContextPath()).append("'/img/db.png',").append("tags:").append("['").append(entry.getValue()).append("']");
            Long dbSize = entry.getValue();
            if (dbSize > 0) {
                ScanResult<String> scanResult = redisService.getKeysByDb(entry.getKey(), cursor);
                sb.append(",");
                sb.append("nodes:").append("[");
                Map<String, String> typeMap = redisService.getType(entry.getKey(), scanResult.getResult());
                typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(request.getContextPath()).append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
                if (dbSize > ServerConstant.PAGE_NUM) {
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
            encode = URLEncoder.encode(jsonString, ServerConstant.ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Cookie cookie = new Cookie(ServerConstant.REDIS_CURSOR, encode);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
        return sb.toString();
    }

    private String dbJson(Integer db, String cursor, String upCursor, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        ScanResult<String> scanResult = redisService.getKeysByDb(db, cursor);
        sb.append("[");
        Map<String, String> typeMap = redisService.getType(db, scanResult.getResult());
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
