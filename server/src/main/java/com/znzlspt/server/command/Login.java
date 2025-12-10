package com.znzlspt.server.command;

import com.znzlspt.dao.model.LocalUser;
import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.MyUser;
import com.znzlspt.server.service.ServerCommandService;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import reactor.core.publisher.Mono;

/**
 * CommandID LOGIN = 100
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Login extends ServerCommandService {
    @Override
    public boolean execute(Message request) {
        Channel channel = getChannel(request);

        String id = request.getString();
        String credential = request.getString();

        dao.loginUser(id, credential)
                .subscribe(u -> {
                    MyUser myUser = new MyUser();
                    myUser.setUUID(u.getUUID());
                    myUser.setNick(u.getNick());
                    myUser.setChannel(channel);
                    channel.attr(AttributeKey.valueOf("user")).set(myUser);

                    Message response = Message.create();
                    response.init()
                            .setCommand(request.getCommand())
                            .addByte((byte) 0)
                            .addString(myUser.getNick())
                            .finalizeBuffer();
                    myUser.send(response);
                });
        return true;
    }
}
