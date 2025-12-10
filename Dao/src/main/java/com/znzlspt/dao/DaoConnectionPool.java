package com.znzlspt.dao;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DaoConnectionPool {

    @Value("${spring.r2dbc.url:r2dbc:mssql://tester:nmklop90@localhost:1433/test}")
    private String r2dbcUrl;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory delegate = ConnectionFactories.get(r2dbcUrl);
        ConnectionPoolConfiguration config = ConnectionPoolConfiguration.builder(delegate)
                .initialSize(5)
                .maxSize(20)
                .maxIdleTime(Duration.ofMinutes(30))
                .build();

        return new ConnectionPool(config);
    }
}
