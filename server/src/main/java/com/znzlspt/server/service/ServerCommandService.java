package com.znzlspt.server.service;

import com.znzlspt.dao.DaoModule;
import com.znzlspt.netcore.command.Command;
import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.service.command.ResponseCommand;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.function.Supplier;

public abstract class ServerCommandService extends Command {

    protected ChannelGroup channelGroup;
    protected Map<Short, Supplier<? extends ServerCommandService>> functions;
    protected DaoModule dao;

    public void setFunctions(Map<Short, Supplier<? extends ServerCommandService>> functions) {
        this.functions = functions;
    }

    public ChannelGroup getChannelGroup() {
        return this.channelGroup;
    }

    public void setChannelGroup(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    public void setDao(DaoModule dao) {
        this.dao = dao;
    }


    /**
     * CommandService를 상속받은 모든 클래스는 execute 메소드를 구현하여 기능을 처리하게 됩니다.
     *
     * @param request
     * @return
     */
    public abstract boolean execute(Message request);

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    private Message createError(short commandId, byte errorCode) {
        Message response = Message.create();
        response.init();
        response.setCommand(ResponseCommand.ERROR);
        response.addShort(commandId);
        response.addByte(errorCode);
        return response;
    }

    protected Channel getChannel(Message message) {
        return message.getChannel();
    }

    protected Object getUser(Message message) {
        return message.getChannel().attr(AttributeKey.valueOf("user")).get();
    }

    protected void sendError(Message message, int errorCode) {
        Channel channel = message.getChannel();

        if (channel != null) {
            channel.writeAndFlush(createError(message.getCommand(), (byte) errorCode));
        }
    }

    protected void broadcast(Message message) {
        if (this.channelGroup != null) {
            this.channelGroup.writeAndFlush(message);
        }
    }


}
