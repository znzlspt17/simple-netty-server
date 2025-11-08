package com.znzlspt.server.service;

import java.time.*;
import java.time.format.DateTimeFormatter;

public abstract class ServerTime {
    private ServerTime() {
    }

    // 현재 UTC 시간 (epoch milliseconds)
    public static long nowInstant() {
        return Instant.now().toEpochMilli();
    }

    // epoch milliseconds (UTC 기준)
    public static long nowEpochMilli() {
        return Instant.now().toEpochMilli();
    }

    // 시간 설정 로직을 수행한 뒤 현재 시간 반환 (epoch milliseconds)
    public static long setTime() {
        // 시간 설정 로직 구현 위치
        return Instant.now().toEpochMilli();
    }

    // UTC 기준 현재 시간 (epoch milliseconds)
    public static long nowUtcIso() {
        return Instant.now().toEpochMilli();
    }

    // 지정한 ZoneId의 현재 시각 (epoch milliseconds)
    public static long nowOffsetIso(ZoneId zone) {
        return ZonedDateTime.now(zone).toInstant().toEpochMilli();
    }

    // 시스템 기본 타임존의 LocalDateTime을 epoch milliseconds로 반환
    public static long nowSystemLocal() {
        return ZonedDateTime.now(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // 지정한 ZoneId의 LocalDateTime을 epoch milliseconds로 반환
    public static long nowLocal(ZoneId zone) {
        return ZonedDateTime.now(zone).toInstant().toEpochMilli();
    }

    // GMT+9의 현재 시각을 epoch milliseconds로 반환
    public static long nowGmtPlus9Iso() {
        return ZonedDateTime.now(ZoneOffset.ofHours(9)).toInstant().toEpochMilli();
    }

    // GMT+9의 현재 시각을 epoch milliseconds로 반환 (이전의 GMT 리터럴 문자열 메서드 대신 long 반환)
    public static long nowGmtPlus9Gmt() {
        return ZonedDateTime.now(ZoneOffset.ofHours(9)).toInstant().toEpochMilli();
    }
}
