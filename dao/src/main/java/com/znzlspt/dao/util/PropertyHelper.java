package com.znzlspt.dao.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertyHelper {
    private static final Logger logger = LoggerFactory.getLogger(PropertyHelper.class);

    private final static String props = "dao.properties";
    private static String type = "mssql";
    private static String id = "tester";
    private static String pwd = "nmklop90";
    private static String host = "localhost";
    private static String port = "1433";
    private static String name = "test";

    private static void loadProperties() {
        Properties properties = new Properties();


        try (InputStream in = PropertyHelper.class.getClassLoader().getResourceAsStream(props)) {
            if (in != null) {
                properties.load(in);
                type = properties.getProperty("db.type", type);
                id = properties.getProperty("db.id", id);
                pwd = properties.getProperty("db.pwd", pwd);
                host = properties.getProperty("db.host", host);
                port = properties.getProperty("db.port", port);
                name = properties.getProperty("db.name", name);
            }
        } catch (IOException ie) {
            logger.error("property load error use default, cause : {}", ie.getMessage());
        }
    }

    public static String getUrl() {
        return String.format("r2dbc:%s://%s:%s@%s:%s/%s", type, id, pwd, host, port, name);
    }
}
