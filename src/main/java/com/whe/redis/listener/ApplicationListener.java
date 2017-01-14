package com.whe.redis.listener;


import com.whe.redis.util.JedisFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by wang hongen on 2017/1/12.
 * 监听服务器启动
 */
@WebListener()
public class ApplicationListener implements ServletContextListener {

    public ApplicationListener() {
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        JedisFactory.init();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        JedisFactory.destroy();
    }
}
