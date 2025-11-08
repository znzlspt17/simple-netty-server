package com.znzlspt.server;

import com.znzlspt.netcore.message.Message;
import io.netty.channel.Channel;

import java.util.UUID;

public class MyUser {
    private Channel channel;
    private UUID uuid;
    private String nick;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void send(Message message) {
        channel.writeAndFlush(message);
    }
}
