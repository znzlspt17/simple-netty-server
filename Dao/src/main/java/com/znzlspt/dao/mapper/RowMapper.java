package com.znzlspt.dao.mapper;


import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@FunctionalInterface
public interface RowMapper<T> {
    T map(Row row, RowMetadata meta);
}
