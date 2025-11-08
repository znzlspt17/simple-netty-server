package com.znzlspt.server.service;

import com.znzlspt.netcore.message.Message;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 수신된 메시지를 적절한 커맨드 처리기로 전달하는 디스패처입니다.
 */
public class CommandDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandDispatcher.class);

    private final CommandRegistry registry;
    private final ChannelGroup channelGroup;

    public CommandDispatcher(CommandRegistry registry, ChannelGroup channelGroup) {
        this.registry = registry;
        this.channelGroup = channelGroup;
    }

    public boolean dispatch(Message message) {
        CommandService handler;
        try {
            handler = registry.create(message.getCommand());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        handler.setChannelGroup(channelGroup);
        handler.setFunctions(registry.view());

        try {
            handler.execute(message);
        } catch (RuntimeException ex) {
            logger.error("Command execution failed | command: {}, cause: {}", message.getCommand(), ex.getMessage());
            throw ex;
        }
        return true;
    }
}
