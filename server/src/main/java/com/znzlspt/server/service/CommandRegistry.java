package com.znzlspt.server.service;

import com.znzlspt.server.command.*;
import com.znzlspt.server.service.command.RequestCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 커맨드 ID와 실행할 구현체 생성을 매핑하는 레지스트리입니다.
 */
public class CommandRegistry {

    private final Map<Short, Supplier<? extends CommandService>> registry = new HashMap<>();

    public CommandRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        register(RequestCommand.LOGIN, Login::new);
        register(RequestCommand.LOGOUT, Logout::new);
        register(RequestCommand.TIME_SYNC, TimeSync::new);
        register(RequestCommand.CHAT_ECHO, Echo::new);
        register(RequestCommand.CHAT_ALL, Say::new);
    }

    public CommandRegistry register(short commandId, Supplier<? extends CommandService> supplier) {
        registry.put(commandId, supplier);
        return this;
    }

    public CommandService create(short commandId) {
        Supplier<? extends CommandService> supplier = registry.get(commandId);
        if (supplier == null) {
            throw new IllegalArgumentException("Unsupported command id: " + commandId);
        }
        return supplier.get();
    }

    public Map<Short, Supplier<? extends CommandService>> view() {
        return Collections.unmodifiableMap(registry);
    }
}
