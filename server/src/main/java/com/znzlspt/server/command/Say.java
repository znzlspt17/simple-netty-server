package com.znzlspt.server.command;

import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.service.CommandServiceImpl;
import io.netty.channel.group.ChannelGroup;

/**
 * CommandID CHAT_SAY = 101
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Say extends CommandServiceImpl {

    @Override
    public boolean execute(Message request) {

        String who = getUser(request);
        short command = request.getCommand();
        String text = request.getString();

        ChannelGroup channelGroup = getChannelGroup();

        Message response = Message.create();
        response.init();
        response.setCommand(command);
        response.addString(who + " : " + text);

        channelGroup.writeAndFlush(response);

        return false;
    }
}
