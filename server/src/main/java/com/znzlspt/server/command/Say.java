package com.znzlspt.server.command;

import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.MyUser;
import com.znzlspt.server.service.CommandService;
import io.netty.channel.group.ChannelGroup;

import java.util.UUID;

/**
 * CommandID CHAT_SAY = 111
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Say extends CommandService {

    @Override
    public boolean execute(Message message) {

        MyUser myUser = (MyUser) getUser(message);
        short command = message.getCommand();
        short type = message.getShort();
        String chat = message.getString();

        UUID uuid = myUser.getUUID();
        String nick = myUser.getNick();


        dao.insertChatLog(uuid, type, chat);

        ChannelGroup channelGroup = this.getChannelGroup();

        Message response = Message.create();
        response.init()
                .setCommand(command)
                .addString(nick)
                .addString(chat)
                .finalizeBuffer();

        broadcast(response);

        return false;
    }
}
