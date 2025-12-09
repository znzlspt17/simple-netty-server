package com.znzlspt.netcore.command;

public enum ResponseCommand {

    OK(0);

    private final int code;

    ResponseCommand(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
