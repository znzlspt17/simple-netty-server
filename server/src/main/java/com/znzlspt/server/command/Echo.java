package com.znzlspt.server.command;

import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.service.CommandServiceImpl;
import io.netty.channel.Channel;

/**
 * CommandID CHAT_ECHO = 100
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Echo extends CommandServiceImpl {

    @Override
    public boolean execute(Message message) {

        String who = getUser(message);
        short command = message.getCommand();
        String text = message.getString();

        Channel channel = message.getChannel();

        Message response = Message.create();
        response.init();
        response.setCommand(command);
        response.addString(who + " : " + text);

        channel.writeAndFlush(response);

        return true;
    }

}