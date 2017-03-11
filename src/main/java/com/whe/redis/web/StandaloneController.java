package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.whe.redis.service.StandAloneService;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by wang hongen on 2017/1/12.
 * Redis控制台
 *
 * @author wanghongen
 */
@Controller
@RequestMapping("/standalone")
public class StandaloneController {
    private static final Logger log = LoggerFactory.getLogger(StandaloneController.class);

    @Resource
    private StandAloneService standAloneService;


    /**
     * 入口 首页
     *
     * @param model model
     * @return index
     */
    @RequestMapping(value = {"/index"})
    public String index(Model model, @RequestParam(defaultValue = "0") String cursor, String match, HttpServletRequest request, HttpServletResponse response) {
        try {
            String treeJson = treeJson(cursor, match, request, response);
            Integer size = standAloneService.getDataBasesSize();
            model.addAttribute("dataSize", size);
            model.addAttribute("tree", treeJson);
            model.addAttribute("match", match);
            model.addAttribute("server", "/standalone");
        } catch (Exception e) {
            e.printStackTrace();
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
                        return standAloneService.setNxSerialize(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return standAloneService.hSetNxSerialize(redis_data_size, redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return standAloneService.lPushSerialize(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return standAloneService.sAddSerialize(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return standAloneService.zAddSerialize(redis_data_size, redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                }
            } else {
                switch (redis_type) {
                    case ServerConstant.REDIS_STRING:
                        return standAloneService.setNx(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_HASH:
                        return standAloneService.hSetNx(redis_data_size, redis_key, redis_field, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_LIST:
                        return standAloneService.lPush(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_SET:
                        return standAloneService.sAdd(redis_data_size, redis_key, redis_value) == 1 ? "1" : "2";
                    case ServerConstant.REDIS_ZSET:
                        return standAloneService.zAdd(redis_data_size, redis_key, redis_score, redis_value) == 1 ? "1" : "2";
                }
            }
        } catch (Exception e) {
            log.error("StandaloneController save error:" + e.getMessage(), e);
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
        return standAloneService.getString(db, key);
    }

    @RequestMapping(value = {"/serialize/getString"})
    @ResponseBody
    public String getSerializeString(Integer db, String key) {
        try {
            return standAloneService.getStringSerialize(db, key);
        } catch (UnsupportedEncodingException e) {
            log.error("StandaloneController getSerializeString error:" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * ajax分页加载list类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getList"})
    @ResponseBody
    public Page<List<String>> getList(int db, String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = standAloneService.findListPageByKey(db, key, pageNo);
        page.pageViewAjax(request.getContextPath() + "/getList", "");
        return page;
    }

    @RequestMapping(value = {"/serialize/getList"})
    @ResponseBody
    public Page<List<String>> getSerializeList(int db, String key, int pageNo, HttpServletRequest request) {
        Page<List<String>> page = null;
        try {
            page = standAloneService.findListPageByKeySerialize(db, key, pageNo);
            page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        } catch (UnsupportedEncodingException e) {
            log.error("StandaloneController getSerializeList error:" + e.getMessage(), e);
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
        return standAloneService.getSet(db, key);
    }

    @RequestMapping(value = {"/serialize/getSet"})
    @ResponseBody
    public Set<String> getSerializeSet(int db, String key) {
        return standAloneService.getSetSerialize(db, key);
    }

    /**
     * ajax加载zSet类型数据
     *
     * @return map
     */
    @RequestMapping(value = {"/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getZSet(int db, int pageNo, String key) {
        Page<Set<Tuple>> page = standAloneService.findZSetPageByKey(db, pageNo, key);
        page.pageViewAjax("/getZSet", "");
        return page;
    }

    @RequestMapping(value = {"/serialize/getZSet"})
    @ResponseBody
    public Page<Set<Tuple>> getSerializeZSet(int db, String key, int pageNo, HttpServletRequest request) {
        Page<Set<Tuple>> page = null;
        try {
            page = standAloneService.findZSetPageByKeySerialize(db, key, pageNo);
            page.pageViewAjax(request.getContextPath() + "/serialize/getList", "");
        } catch (UnsupportedEncodingException e) {
            log.error("StandaloneController getSerializeZSet error:" + e.getMessage(), e);
        }
        return page;
    }

    @RequestMapping(value = {"/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAll(int db, String key) {
        return standAloneService.hGetAll(db, key);
    }

    @RequestMapping(value = {"/serialize/hGetAll"})
    @ResponseBody
    public Map<String, String> hGetAllSerialize(int db, String key) {
        try {
            return standAloneService.hGetAllSerialize(db, key);
        } catch (UnsupportedEncodingException e) {
            log.error("StandaloneController hGetAllSerialize error:" + e.getMessage(), e);
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
            return standAloneService.renameNx(db, oldKey, newKey) == 0 ? "2" : "1";
        } catch (Exception e) {
            log.error("StandaloneController renameNx error:" + e.getMessage(), e);
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
            return standAloneService.ttl(db, key);
        } catch (Exception e) {
            log.error("StandaloneController ttl error:" + e.getMessage(), e);
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

            standAloneService.setExpire(db, key, seconds);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController setExpire error:" + e.getMessage(), e);
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
            standAloneService.persist(db, key);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController persist error:" + e.getMessage(), e);
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
            standAloneService.delKey(db, key);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController delKey error:" + e.getMessage(), e);
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
            standAloneService.set(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateString error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping("/serialize/updateString")
    @ResponseBody
    public String updateStringSerialize(int db, String key, String val) {
        try {
            standAloneService.setSerialize(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateStringSerialize error:" + e.getMessage(), e);
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
            standAloneService.lSet(db, index, key, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateList error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateList"})
    @ResponseBody
    public String updateListSerialize(int db, int index, String key, String val) {
        try {
            standAloneService.lSetSerialize(db, index, key, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController ResponseBody error:" + e.getMessage(), e);
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
            long lLen = standAloneService.lLen(db, key);
            if (listSize != lLen) {
                return "2";
            }
            standAloneService.lRem(db, index, key);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController delList error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = {"/updateSet"})
    @ResponseBody
    public String updateSet(int db, String key, String oldVal, String newVal) {
        try {
            standAloneService.updateSet(db, key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateSet"})
    @ResponseBody
    public String updateSetSerialize(int db, String key, String oldVal, String newVal) {
        try {
            standAloneService.updateSetSerialize(db, key, oldVal, newVal);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delSet"})
    @ResponseBody
    public String delSet(int db, String key, String val) {
        try {
            standAloneService.delSet(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController delSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateZSet"})
    @ResponseBody
    public String updateZSet(int db, String key, String oldVal, String newVal, double score) {
        try {
            standAloneService.updateZSet(db, key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateZSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateZSet"})
    @ResponseBody
    public String updateZSetSerialize(int db, String key, String oldVal, String newVal, double score) {
        try {
            standAloneService.updateZSetSerialize(db, key, oldVal, newVal, score);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController updateZSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delZSet"})
    @ResponseBody
    public String delZSet(int db, String key, String val) {
        try {
            standAloneService.delZSet(db, key, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController delZSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/hSet"})
    @ResponseBody
    public String hSet(int db, String key, String field, String val) {
        try {
            standAloneService.hSet(db, key, field, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController hSet error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/hSet"})
    @ResponseBody
    public String hSetSerialize(int db, String key, String field, String val) {
        try {
            standAloneService.hSetSerialize(db, key, field, val);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController hSetSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/updateHash"})
    @ResponseBody
    public String updateHash(int db, String key, String oldField, String newField, String val) {
        try {
            return standAloneService.updateHash(db, key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            log.error("StandaloneController updateHash error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/serialize/updateHash"})
    @ResponseBody
    public String updateHashSerialize(int db, String key, String oldField, String newField, String val) {
        try {
            return standAloneService.updateHashSerialize(db, key, oldField, newField, val) ? "1" : "2";
        } catch (Exception e) {
            log.error("StandaloneController updateHashSerialize error:" + e.getMessage(), e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = {"/delHash"})
    @ResponseBody
    public String delHash(int db, String key, String field) {
        try {
            standAloneService.delHash(db, key, field);
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController delHash error:" + e.getMessage(), e);
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
    public void backup(HttpServletResponse response) throws IOException {
        String str = standAloneService.backup();
        LocalDate data = LocalDate.now();
        log.info("StandaloneController backup info:" + data);
        response.setContentType("text/plain; charset=utf-8");//设置MIME类型
        response.setHeader("Content-Disposition", "attachment; filename=" + data + "standalone.redis");
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
                        nowMap = map;
                        db = 0;
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_STRING)) {
                        standAloneService.saveAllString(db, (Map<String, String>) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_HASH)) {
                        standAloneService.saveAllHash(db, (Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_LIST)) {
                        standAloneService.saveAllList(db, (Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_SET)) {
                        standAloneService.saveAllSet(db, (Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_ZSET)) {
                        standAloneService.saveAllZSet(db, (Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("StandaloneController recover error:" + e.getMessage(), e);
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
                        db = 0;
                        isCluster = true;
                        nowMap = map;
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_STRING)) {
                        standAloneService.saveAllStringSerialize(db, (Map<String, String>) nowMap.get(ServerConstant.REDIS_STRING));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_LIST)) {
                        standAloneService.saveAllListSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_LIST));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_SET)) {
                        standAloneService.saveAllSetSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_SET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_ZSET)) {
                        standAloneService.saveAllZSetSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_ZSET));
                    }
                    if (nowMap.containsKey(ServerConstant.REDIS_HASH)) {
                        standAloneService.saveAllHashSerialize(db, (Map) nowMap.get(ServerConstant.REDIS_HASH));
                    }
                    if (isCluster) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("StandaloneController serializeRecover error:" + e.getMessage(), e);
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
            standAloneService.flushAll();
            return "1";
        } catch (Exception e) {
            log.error("StandaloneController flushAll error:" + e.getMessage(), e);
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
    public String nextPage(Integer db, String cursor, String match, HttpServletRequest request, HttpServletResponse
            response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            Optional<Cookie> cookie = Stream.of(cookies).filter(c -> c.getName().equals(ServerConstant.REDIS_CURSOR)).findAny();
            cookie.ifPresent(c -> {
                try {
                    String value = URLDecoder.decode(c.getValue(), ServerConstant.CHARSET);
                    Map map = JSON.parseObject(value, Map.class);
                    List<String> list = (List<String>) map.get(db);
                    list.add(cursor);
                    c.setValue(URLEncoder.encode(JSON.toJSONString(map), ServerConstant.CHARSET));
                    response.addCookie(c);
                } catch (UnsupportedEncodingException e) {
                    log.error("StandaloneController nextPage error:" + e.getMessage(), e);
                }
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
            int size = list.size();
            return IntStream
                    .range(0, size)
                    .filter(i -> list.get(i).equals(cursor))
                    .boxed()
                    .map(i -> list.get(i - 1))
                    .findAny().orElse(ServerConstant.DEFAULT_CURSOR);
        }).orElse(ServerConstant.DEFAULT_CURSOR);
    }

    private String treeJson(String cursor, String match, HttpServletRequest request, HttpServletResponse response) {
        Map<Integer, Long> dataBases = standAloneService.getDataBases();
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("text:").append("'").append(JedisFactory.getStandAlone()).append("',");
        sb.append("icon:'").append(request.getContextPath()).append("/img/redis.png',").append("expanded:").append(true).append(",");
        sb.append("nodes:").append("[");
        Map<Integer, List<String>> map = new HashMap<>();
        dataBases.entrySet().forEach(entry -> {
            sb.append("{text:").append("'").append(ServerConstant.DB).append(entry.getKey()).append("',")
                    .append("icon:'").append(request.getContextPath()).append("/img/db.png',")
                    .append("tags:").append("['").append(entry.getValue()).append("']");
            Long dbSize = entry.getValue();
            if (dbSize > 0) {
                ScanResult<String> scanResult = standAloneService.getKeysByDb(entry.getKey(), cursor, match);
                sb.append(",").append("expanded:").append(true).append(",").append("nodes:").append("[");
                Map<String, String> typeMap = standAloneService.getType(entry.getKey(), scanResult.getResult());
                typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(request.getContextPath())
                        .append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
                if (scanResult.getResult().size() >= ServerConstant.PAGE_NUM) {
                    List<String> list = new ArrayList<>();
                    list.add("0");
                    map.put(entry.getKey(), list);
                    String stringCursor = scanResult.getStringCursor();
                    sb.append("{page:").append("'<ul class=\"pagination\" style=\"margin:0px\"> <li class=\"disabled\" ><a  href=\"javascript:void(0);\" onclick=\"upPage(")
                            .append(entry.getKey()).append(",").append(ServerConstant.DEFAULT_CURSOR).append(",event)").append(" \">上一页</a></li><li ");
                    if ("0".equals(stringCursor)) {
                        sb.append(" class=\"disabled\" ");
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
            log.error("StandaloneController treeJson error:" + e.getMessage(), e);
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
        ScanResult<String> scanResult = standAloneService.getKeysByDb(db, cursor, match);
        sb.append("[");
        Map<String, String> typeMap = standAloneService.getType(db, scanResult.getResult());
        typeMap.forEach((key, type) -> sb.append("{text:").append("'").append(key).append("',icon:'").append(request.getContextPath()).append("/img/").append(type).append(".png").append("',type:'").append(type).append("'},"));
        String stringCursor = scanResult.getStringCursor();
        sb.append("{page:").append("'<ul class=\"pagination\" style=\"margin:0px\"> <li ");
        if (cursor == null || "0".equals(cursor)) {
            sb.append(" class=\"disabled\" ");
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
