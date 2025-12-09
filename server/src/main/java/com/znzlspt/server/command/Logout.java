package com.znzlspt.server.command;

import com.znzlspt.dao.UserDao;
import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.MyUser;
import com.znzlspt.server.service.CommandService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * CommandID LOGOUT = 101
 * commandService.execute(message); 을 통해 실행되는 클래스입니다.
 */

public class Logout extends CommandService {

    Logger logger = LoggerFactory.getLogger(Logout.class);

    @Override
    public boolean execute(Message request) {
        Channel channel = getChannel(request);
        MyUser user = (MyUser) getUser(request);

        Channel storedChannel = user.getChannel();
        UUID uuid = user.getUUID();

        UserDao userDao = (UserDao) daoModule.getUserDao();

        if (channel.equals(user.getChannel())) {
            channelGroup.remove(channel);
            userDao.logoutUser(uuid);
            channel.close();

            return false;
        } else {

            channelGroup.remove(storedChannel);
            channelGroup.remove(channel);
            userDao.logoutUser(uuid);
            storedChannel.disconnect();
            channel.close();

            logger.error("Error: The requested channel does not match the channel stored in the user object. : {}", uuid);

            return true;
        }

    }
}
