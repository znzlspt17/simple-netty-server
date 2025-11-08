package com.znzlspt.dao.mapper;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Mono;

public final class R2dbcMapper {
    private R2dbcMapper() {
    }

    public static <T> Flux<T> mapMulti(Flux<? extends Result> results, RowMapper   <T> mapper) {
        return Flux.from(results).flatMap(result -> result.map(mapper::map));
    }

    public static <T> Mono<T> mapSingle(Mono<? extends Result> resultMono, RowMapper<T> mapper) {
        return Mono.from(resultMono).flatMap(result -> Mono.from(result.map(mapper::map)));
    }

    public static <T> Flux<T> executeAndMulti(Connection connection, String sql, RowMapper<T> mapper, Object... params) {
        Statement stmt = connection.createStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.bind("p" + (i + 1), params[i]);
        }
        return R2dbcMapper.mapMulti(Flux.from(stmt.execute()), mapper);
    }

    public static <T> Mono<T> executeAndSingle(Connection connection, String sql, RowMapper<T> mapper, Object... params) {
        Statement stmt = connection.createStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.bind("p" + (i + 1), params[i]);
        }
        return R2dbcMapper.mapSingle(Mono.from(stmt.execute()), mapper);
    }
}
