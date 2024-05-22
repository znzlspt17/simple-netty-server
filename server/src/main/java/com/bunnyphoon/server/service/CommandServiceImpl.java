package com.bunnyphoon.server.service;

import com.bunnyphoon.netcore.command.Command;
import com.bunnyphoon.netcore.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.Map;

public abstract class CommandServiceImpl extends Command {

    protected ChannelGroup channelGroup;
    protected Map<Short, String> functions;

    public void setFunctions(Map<Short, String> functions) {
        this.functions = functions;
    }

    public ChannelGroup getChannelGroup() {
        return this.channelGroup;
    }

    /**
     * CommandSerivce를 상속받은 모든 클래스는 execute 메소드를 구현하여 기능을 처리하게 됩니다.
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

    protected String getUser(Message message) {
        return message.getChannel().remoteAddress().toString() + "@" + message.getChannel().id().asShortText();
    }

    protected void sendError(Message message, int errorCode) {
        Channel channel = message.getChannel();

        if (channel != null) {
            channel.write(createError(message.getCommand(), (byte) errorCode));
        }
    }


}
