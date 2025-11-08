package com.znzlspt.server.command;

import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.MyUser;
import com.znzlspt.server.service.CommandService;
import com.znzlspt.server.service.ServerTime;


/**
 * CommandID TIME_SYNC = 102
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class TimeSync extends CommandService {
    @Override
    public boolean execute(Message request) {
        MyUser myUser = (MyUser) getUser(request);

        Message response = Message.create();
        response.init()
                .setCommand(request.getCommand())
                .addLong(ServerTime.nowGmtPlus9Gmt())
                .finalizeBuffer();
        myUser.send(response);

        return false;
    }
}
