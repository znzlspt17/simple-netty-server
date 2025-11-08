package com.znzlspt;

import com.znzlspt.dao.DaoModule;
import com.znzlspt.server.netty.SimpleNettyRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        DaoModule dao = DaoModule.getInstance();

        logger.info("Start Simple Netty Server");
        SimpleNettyRunner simpleNetty = new SimpleNettyRunner();
        simpleNetty.start(20999);
    }
}
