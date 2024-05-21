package com.bunnyphoon.netcore.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * Netty 에서 Message 와 ByteBuf 를 서로 변환 가능하도록 하는 코덱입니다.
 */
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
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
