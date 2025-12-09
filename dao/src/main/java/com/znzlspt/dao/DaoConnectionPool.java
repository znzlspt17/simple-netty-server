package com.znzlspt.dao;

import com.znzlspt.dao.util.PropertyHelper;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;

import java.time.Duration;

public class DaoConnectionPool {
    private DaoConnectionPool() {}

    private static final ConnectionPool CP = createPool();

    private static ConnectionPool createPool() {
        ConnectionFactory delegate = ConnectionFactories.get(PropertyHelper.getUrl());
        ConnectionPoolConfiguration CPConfig = ConnectionPoolConfiguration.builder(delegate)
                .initialSize(5)
                .maxSize(20)
                .maxIdleTime(Duration.ofMinutes(30))
                .build();

        return new ConnectionPool(CPConfig);
    }

    static ConnectionFactory connectionFactory() {
        return CP;
    }
}
