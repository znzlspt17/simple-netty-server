package com.znzlspt.server.service.command;

/**
 * 클라이언트 요청 커맨드 ID를 정의합니다.
 */
public final class RequestCommand {

    private RequestCommand() {}

    public static final short LOGIN = 100;
    public static final short LOGOUT = 101;
    public static final short TIME_SYNC = 102;
    public static final short CHAT_ECHO = 110;
    public static final short CHAT_ALL = 111;
}
