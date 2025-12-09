package com.znzlspt.netcore.message;

public interface CommandDispatcher {
    boolean dispatch(Message request);
}
