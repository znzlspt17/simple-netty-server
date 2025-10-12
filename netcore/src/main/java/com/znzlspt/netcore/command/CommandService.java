package com.znzlspt.netcore.command;

import com.znzlspt.netcore.message.Message;

import java.util.Collection;
import java.util.Map;

/**
 * 사용될 프로젝트에서 구현해야될 커맨드 서비스의 원형입니다.
 */
public interface CommandService {
    public abstract void setTable(Map<Integer, Command> table);

    public abstract void addCommand(int key, Command command);

    public abstract Command getCommand(int key);

    public abstract Collection<Command> getCommandList();

    public boolean execute(Message request, boolean setResponseTime);

}