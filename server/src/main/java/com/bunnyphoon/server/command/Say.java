package com.bunnyphoon.server.command;

import com.bunnyphoon.netcore.message.Message;
import com.bunnyphoon.server.service.CommandService;
import io.netty.channel.group.ChannelGroup;

/**
 * CommandID CHAT_SAY = 101
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Say extends CommandService {

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
