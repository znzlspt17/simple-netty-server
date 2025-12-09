package com.znzlspt.server.service;

import com.znzlspt.dao.IDaoModule;
import com.znzlspt.dao.UserDao;
import com.znzlspt.netcore.command.Command;
import com.znzlspt.netcore.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.function.Supplier;

public abstract class CommandService extends Command {


    protected Map<Short, Supplier<? extends CommandService>> functions;

    protected ChannelGroup channelGroup;
    protected IDaoModule daoModule;
    protected UserDao userDao;
    public void setFunctions(Map<Short, Supplier<? extends CommandService>> functions) {
        this.functions = functions;
    }

    public void setChannelGroup(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    public ChannelGroup getChannelGroup() {
        return this.channelGroup;
    }

    public void setDaoModule(IDaoModule daoModule) {
        this.daoModule = daoModule;
        userDao = (UserDao) daoModule.getUserDao();
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
        response.setCommand(0);
        response.addShort(commandId);
        response.addByte(errorCode);
        return response;
    }

    protected Channel getChannel(Message message) {
        return message.getChannel();
    }

    protected Object getUser(Message message) {
        return message.getChannel().attr(AttributeKey.valueOf("MyUser")).get();
    }

    protected void sendError(Message message, int errorCode) {
        Channel channel = message.getChannel();

        if (channel != null) {
            channel.write(createError(message.getCommand(), (byte) errorCode));
        }
    }

    protected void broadcast(Message message) {
        if (this.channelGroup != null) {
            this.channelGroup.writeAndFlush(message);
        }
    }


}
