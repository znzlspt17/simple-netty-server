package com.znzlspt.server.netty;

import com.znzlspt.netcore.handler.InboundHandlerBindHelper;
import com.znzlspt.netcore.handler.ServiceHandler;
import com.znzlspt.netcore.message.Codec;
import com.znzlspt.netcore.message.Message;
import com.znzlspt.server.service.CommandDispatcher;
import com.znzlspt.server.service.CommandRegistry;
import com.znzlspt.server.service.command.ResponseCommand;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SimpleNettyRunner implements ServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleNettyRunner.class);

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final CommandDispatcher commandDispatcher;
    private final CommandRegistry commandRegistry;

    /**
     * Netty 서버사이드의 ServerBootstrap 의 간단한 구성입니다<br>
     * 연결만을 담당하는 bossGroup 과 I/O 및 연결해제를 담당하는 workerGroup 을 ServerBootstrap 에 등록합니다.<br>
     * InboundHandlerBindHelper 를 생성하고 NettyHandler 에 SimpleNetty (ServiceHandler 의 구현) 을 건네줍니다.<br>
     * InboundHandlerBindHelper 는 ChannelInboundHandlerAdapter 를 확장했고 서버부트스트랩의 채널 파이프라인에 inbound 처리자로 등록되어있기 때문에
     * InboundHandlerBindHelper 는 들어오는 모든 이벤트를 SimpleNetty (ServiceHandler 의 구현) 에게 건네주어 이 안에서 메시지에 대한 직접적인 처리를 하게 됩니다.<br>
     * 이런식으로 요청에 대한 핸들러를 직접적으로 사용하지 않고 인터페이스를 경유하여 오게되면 발생한 이벤트, 수신된 메시지 마다 다른 유형의 ServiceHandler 를 구현하여
     * 하나의 클래스에 모든 처리가 몰리게 되는 상황을 방지 할 수 있고 더욱 유연한 코드 작성이 가능하게 됩니다.
     */

    public SimpleNettyRunner(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        this.commandDispatcher = new CommandDispatcher(commandRegistry, channelGroup);
    }

    /**
     * Netty 서버를 시작합니다.
     * @param port 바인딩할 포트 번호
     */
    public void start(int port) {
        InboundHandlerBindHelper inboundHandlerBindHelper = new InboundHandlerBindHelper();
        inboundHandlerBindHelper.setServiceHandler(this);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                .addLast("idleStateHandler", new IdleStateHandler(60, 0, 0))
                .addLast("Codec", new Codec())
                .addLast("nettyHandler", inboundHandlerBindHelper);
                }
            });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            logger.info("Lobby Server START ip = {}, port = {}", future.channel().localAddress(), port);
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 채널이 연결됐을때 호출되는 메소드<br>
     * 채널이 등록됐을 때와는 다르며, 송수신이 가능한 상태일때 활성화 됩니다.
     * @param channelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        channelGroup.add(channelHandlerContext.channel());

        Message message = Message.create();
        message.init();
        message.setCommand(ResponseCommand.OK);
        message.finalizeBuffer();

        logger.info("send message | command : {}, size : {}", message.getCommand(), message.getSize());
        channelHandlerContext.writeAndFlush(message);
    }

    /**
     * 채널의 연결이 끊어졌을 경우 호출되는 메소드<br>
     * 연결된 채널을 관리하는 채널 그룹에서 해당 ChannelHandlerContext 를 제거해줍니다.
     * @param channelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        channelGroup.remove(channelHandlerContext.channel());
    }

    /**
     * 메시지가 수신될 때 호출되는 메소드<br>
     * 수신된 커맨드 값을 기반으로 {@link CommandDispatcher}가 적절한 처리기를 찾아 실행합니다.
     * @param channelHandlerContext
     * @param o 사용자가 정의의 메시지 타입을 MessageCodec의 decode에 변환되어 들어오게 되지만 유연성을 위해서 Object 타입으로 전달됩니다.
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) {
        if (o instanceof Message message) {
            message.setChannel(channelHandlerContext.channel());
            boolean dispatched = commandDispatcher.dispatch(message);
            if (!dispatched) {
                logger.warn("Unsupported command received: {}", message.getCommand());
                sendUnknownCommandError(message);
            }
        }
    }

    /**
     * 사용자 정의 이벤트가 발생 했을때 호출되는 메소드<br>
     * 현재는 부트스트랩의 채널 파이프라인에 IdleStateHandler 가 연결되어있어 일정 시간동안 메시지가 수신되지 않으면 호출됩니다.
     * @param channelHandlerContext
     * @param o 발생된 이벤트가 전달된다
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o) {
        if (o instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = channelHandlerContext.channel();
                logger.info("client no longer request == id : {}, Address : {}", channel.id(), channel.remoteAddress());
            }
        }
    }

    /**
     * 네티의 이벤트 처리 과정에서 에러가 발생하면 호출되는 메소드<br>
     * 에러 발생시 소켓을 닫고 던져진 에러의 종류를 확인하여 대응 코드를 작성하면 됩니다.
     * @param channelHandlerContext
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        channelHandlerContext.close();
        if (cause instanceof IOException) {
            logger.error("NETTY IO EXCEPTION", cause);
            return;
        }
        logger.error("NETTY EXCEPTION", cause);
    }

    private void printStackTrace(Throwable throwable) {
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logger.error("NETTY EXCEPTION | {}", stackTraceElement);
        }
    }

    private void sendUnknownCommandError(Message message) {
        Channel channel = message.getChannel();
        if (channel == null) {
            return;
        }

        Message response = Message.create();
        response.init();
        response.setCommand(ResponseCommand.ERROR);
        response.addString("Unsupported command: " + message.getCommand());

        channel.writeAndFlush(response);
    }
}
