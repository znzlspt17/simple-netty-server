package com.znzlspt;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * OSGi Felix Framework를 시작하는 메인 클래스입니다.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting OSGi Framework...");

        Map<String, Object> config = new HashMap<>();

        // Felix 캐시 디렉토리 설정
        config.put(FelixConstants.FRAMEWORK_STORAGE, "felix-cache");
        config.put(FelixConstants.FRAMEWORK_STORAGE_CLEAN, FelixConstants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        // 시스템 패키지 export (OSGi 컨테이너에서 사용할 JDK 패키지들)
        config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "org.slf4j;version=2.0.17," +
                "org.slf4j.helpers;version=2.0.17," +
                "org.slf4j.spi;version=2.0.17," +
                "io.netty.bootstrap;version=4.2.6," +
                "io.netty.buffer;version=4.2.6," +
                "io.netty.channel;version=4.2.6," +
                "io.netty.channel.nio;version=4.2.6," +
                "io.netty.channel.socket;version=4.2.6," +
                "io.netty.channel.socket.nio;version=4.2.6," +
                "io.netty.channel.group;version=4.2.6," +
                "io.netty.handler.codec;version=4.2.6," +
                "io.netty.handler.timeout;version=4.2.6," +
                "io.netty.util;version=4.2.6," +
                "io.netty.util.concurrent;version=4.2.6," +
                "io.r2dbc.pool;version=1.0.2," +
                "io.r2dbc.spi;version=1.0.3," +
                "reactor.core.publisher;version=3.7.12," +
                "reactor.util.function;version=3.7.12"
        );

        // Felix Framework 생성 및 시작
        Framework framework = new Felix(config);
        try {
            framework.start();
            logger.info("OSGi Framework started successfully");

            // 번들 설치 및 시작
            installAndStartBundles(framework);

            // 프레임워크가 종료될 때까지 대기
            framework.waitForStop(0);
            logger.info("OSGi Framework stopped");

        } catch (BundleException e) {
            logger.error("Failed to start OSGi Framework", e);
            System.exit(1);
        }
    }

    private static void installAndStartBundles(Framework framework) {
        try {
            // Felix SCR 번들을 먼저 설치 (Declarative Services 지원)
            logger.info("Installing Felix SCR bundle for Declarative Services support...");

            // SCR 번들은 클래스패스에서 찾을 수 있음
            // shadowJar로 패키징되면 포함되어 있을 것임

            // 애플리케이션 번들 설치
            String[] bundlePaths = {
                    "dao/build/libs/dao-1.0-SNAPSHOT.jar",
                    "netcore/build/libs/netcore-1.0-SNAPSHOT.jar",
                    "server/build/libs/server-1.0-SNAPSHOT.jar"
            };

            for (String path : bundlePaths) {
                File bundleFile = new File(path);
                if (bundleFile.exists()) {
                    Bundle bundle = framework.getBundleContext().installBundle("file:" + bundleFile.getAbsolutePath());
                    bundle.start();
                    logger.info("Bundle installed and started: {}", bundle.getSymbolicName());
                } else {
                    logger.warn("Bundle file not found: {}", path);
                }
            }

            logger.info("All bundles installed successfully");

        } catch (BundleException e) {
            logger.error("Failed to install/start bundles", e);
        }
    }
}
