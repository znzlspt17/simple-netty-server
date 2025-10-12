package com.znzlspt.netcore.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *
 * Netty의 ChannelInboundHandlerAdapter를 확장하여 클라이언트가 서버에 연결됐을 때, 메시지를 수신했을 때, 이벤트가 발생했을 때 호출되는 콜백 메소드 집합입니다.<br>
 * 실제 사용될 프로젝트에서 ServiceHandler 구현체를 전달해야합니다
 */

public class InboundHandlerBindHelper extends ChannelInboundHandlerAdapter {

    protected ServiceHandler handler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handler.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handler.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handler.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        handler.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handler.exceptionCaught(ctx, cause);
    }

    public void setServiceHandler(ServiceHandler handler) {
        this.handler = handler;
    }

    public ServiceHandler getServiceHandler() {
        return handler;
    }
}
