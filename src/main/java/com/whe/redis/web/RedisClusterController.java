package com.whe.redis.web;

import com.alibaba.fastjson.JSON;
import com.whe.redis.service.RedisClusterService;
import com.whe.redis.util.Page;
import com.whe.redis.util.ServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("text:").append("'").append("redisCluster").append("',");
        sb.append("icon:").append(request.getContextPath()).append("'/img/redis.png',").append("expanded:").append(true).append(",");
        sb.append("nodes:").append("[");
        sb.append("{text:").append("'").append("data").append("',").append("icon:").append(contextPath).append("'/img/db.png',");
        sb.append("nodes:");
        Map<Integer, Map<String, String>> map = new HashMap<>();
        Map<String, String> nodeCursor = new HashMap<>();
        map.put(1, nodeCursor);
        sb.append(dataTree(1, nodeCursor, map));
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
        model.addAttribute("server", "/cluster");
        return "index";
    }


    /*  @RequestMapping("/backup")
      public void backup(HttpServletRequest request, HttpServletResponse response) throws IOException {
          Map<String, Object> map = new HashMap<>();
          Map<String, String> stringMap = stringService.getAllString("*");
          List<Object> list = new ArrayList<>();
          if (stringMap.size() > 0) {
              map.put(ServerConstant.REDIS_STRING, stringMap);
              list.add(map);
          }
          map = new HashMap<>();
          Map<String, List<String>> listMap = listService.getAllList();
          if (listMap.size() > 0) {
              map.put(ServerConstant.REDIS_LIST, listMap);
              list.add(map);
          }
          map = new HashMap<>();
          Map<String, Set<String>> setMap = setService.getAllSet();
          if (setMap.size() > 0) {
              map.put(ServerConstant.REDIS_SET, setMap);
              list.add(map);
          }
          map = new HashMap<>();
          Map<String, Set<Tuple>> tupleMap = ZSetService.getAllZSet();
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
          Map<String, Map<String, String>> hashMap = hashService.getAllHash();
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

  */
    @RequestMapping(value = {"/hSet"})
    @ResponseBody
    public String hSet(String key, String field, String val) {
        try {
            redisClusterService.hSet(key, field, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping(value = {"/updateHash"})
    @ResponseBody
    public String updateHash(String key, String oldField, String newField, String val) {
        try {
            redisClusterService.updateHash(key, oldField, newField, val);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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
            return "0";
        }
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
            return "0";
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
            return "0";
        }
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
            return "0";
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
            return "0";
        }
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
            return "0";
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
            return "0";
        }
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
            return "0";
        }
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
            return "0";
        }
    }

    @RequestMapping("/renameNx")
    @ResponseBody
    public String renameNx(String oldKey, String newKey) {
        try {
            return redisClusterService.renameNx(oldKey, newKey) == 0 ? "2" : "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @RequestMapping("/ttl")
    @ResponseBody
    public long ttl(String key) {
        return redisClusterService.ttl(key);
    }

    @RequestMapping("/setExpire")
    @ResponseBody
    public long setExpire(String key, int seconds) {
        return redisClusterService.expire(key, seconds);
    }

    @RequestMapping("/upPage")
    @ResponseBody
    public String upPage(Integer pageNo, HttpServletRequest request, HttpServletResponse response) {
        return page(pageNo, request, response);
    }

    @RequestMapping("/nextPage")
    @ResponseBody
    public String nextPage(Integer pageNo, HttpServletRequest request, HttpServletResponse response) {
        return page(pageNo, request, response);
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

    private String page(Integer pageNo, HttpServletRequest request, HttpServletResponse response) {
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
                data = dataTree(pageNo, nodeCursor, map);
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

    private String dataTree(int pageNo, Map<String, String> nodeCursor, Map<Integer, Map<String, String>> map) {
        Map<String, ScanResult<String>> nodeScan = redisClusterService.scan(nodeCursor);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Map<String, String> nextNodeCursor = nodeScan.entrySet().stream()
                .filter(entry -> {
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
