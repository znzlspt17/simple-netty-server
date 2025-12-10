package com.znzlspt.server.command;

import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.MyUser;
import com.znzlspt.server.service.ServerCommandService;

/**
 * CommandID CHAT_ECHO = 110
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Echo extends ServerCommandService {

    @Override
    public boolean execute(Message request) {

        MyUser myUser = (MyUser) getUser(request);
        short command = request.getCommand();
        String chat = request.getString();

        Message response = Message.create();
        response.init()
                .setCommand(command)
                .addString(myUser.getNick())
                .addString(chat)
                .finalizeBuffer();

        myUser.send(response);

        return true;
    }

}