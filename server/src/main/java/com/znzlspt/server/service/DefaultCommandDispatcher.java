package com.znzlspt.server.service;

import com.znzlspt.dao.IDaoModule;
import com.znzlspt.netcore.message.CommandDispatcher;
import com.znzlspt.netcore.message.Message;
import io.netty.channel.group.ChannelGroup;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 수신된 메시지를 적절한 커맨드 처리기로 전달하는 디스패처입니다.
 */
@Component(immediate = true)
public class DefaultCommandDispatcher implements CommandDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommandDispatcher.class);

    @Reference
    private CommandRegistry registry;
    @Reference
    private ChannelGroup channelGroup;
    @Reference
    private IDaoModule dao;

    @Override
    public boolean dispatch(Message message) {
        CommandService handler;
        try {
            handler = registry.create(message.getCommand());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        if (handler != null) {
            handler.setChannelGroup(channelGroup);
            handler.setFunctions(registry.view());
            handler.setDaoModule(dao);
        }

        try {
            handler.execute(message);
        } catch (RuntimeException ex) {
            logger.error("Command execution failed | command: {}, cause: {}", message.getCommand(), ex.getMessage());
            throw ex;
        }
        return true;
    }
}
