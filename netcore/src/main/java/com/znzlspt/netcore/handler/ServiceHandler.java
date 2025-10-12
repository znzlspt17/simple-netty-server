package com.znzlspt.netcore.handler;

import io.netty.channel.ChannelHandlerContext;

/**
 * 사용될 프로젝트에서 구현해야될 서비스핸들러의 원형입니다.
 */
public interface ServiceHandler {
    public void channelActive(ChannelHandlerContext ctx) throws Exception;

    public void channelInactive(ChannelHandlerContext ctx) throws Exception;

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception;

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
