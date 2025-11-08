package com.znzlspt.server.service.command;

/**
 * 서버 응답용 커맨드 ID를 정의합니다.
 */
public final class ResponseCommand {

    private ResponseCommand(){}

    public static final short OK = 0;
    public static final short ERROR = -1;
}
