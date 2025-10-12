package com.znzlspt.netcore.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class Codec extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> out) {
        ByteBuf encoded = message.getBuffer().copy();
        out.add(encoded);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        Message message = Message.create();
        message.setBuffer(byteBuf.copy());
        out.add(message);
    }
}