package com.znzlspt.server;

import com.znzlspt.server.netty.SimpleNettyRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.znzlspt.server", "com.znzlspt.dao"})
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner runNettyServer(SimpleNettyRunner nettyRunner) {
        return args -> {
            logger.info("Start Simple Netty Server");
            nettyRunner.start(20999);
        };
    }
}
