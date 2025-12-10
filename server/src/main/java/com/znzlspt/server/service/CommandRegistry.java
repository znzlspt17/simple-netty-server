package com.znzlspt.server.service;

import com.znzlspt.dao.DaoModule;
import com.znzlspt.server.command.*;
import com.znzlspt.server.service.command.RequestCommand;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 커맨드 ID와 실행할 구현체 생성을 매핑하는 레지스트리입니다.
 * Command 인스턴스는 요청마다 새로 생성됩니다.
 */
@Service
public class CommandRegistry {

    private final Map<Short, Supplier<? extends ServerCommandService>> registry = new HashMap<>();
    private final DaoModule daoModule;

    public CommandRegistry(DaoModule daoModule) {
        this.daoModule = daoModule;
        registerDefaults();
    }
    
    public DaoModule getDaoModule() {
        return daoModule;
    }

    private void registerDefaults() {
        register(RequestCommand.LOGIN, Login::new);
        register(RequestCommand.LOGOUT, Logout::new);
        register(RequestCommand.TIME_SYNC, TimeSync::new);
        register(RequestCommand.CHAT_ECHO, Echo::new);
        register(RequestCommand.CHAT_ALL, Say::new);
    }

    public CommandRegistry register(short commandId, Supplier<? extends ServerCommandService> supplier) {
        registry.put(commandId, supplier);
        return this;
    }

    public ServerCommandService create(short commandId) {
        Supplier<? extends ServerCommandService> supplier = registry.get(commandId);
        if (supplier == null) {
            throw new IllegalArgumentException("Unsupported command id: " + commandId);
        }
        return supplier.get();
    }

    public Map<Short, Supplier<? extends ServerCommandService>> view() {
        return Collections.unmodifiableMap(registry);
    }
}
