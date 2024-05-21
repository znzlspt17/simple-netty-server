package com.bunnyphoon.server.netty;

import com.bunnyphoon.netcore.handler.InboundHandlerBindUtil;
import com.bunnyphoon.netcore.handler.ServiceHandler;
import com.bunnyphoon.netcore.message.Message;
import com.bunnyphoon.netcore.message.MessageCodec;
import com.bunnyphoon.server.service.CommandIDs;
import com.bunnyphoon.server.service.CommandService;
import com.bunnyphoon.server.service.CommandUtil;
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

import java.io.IOException;

public class SimpleNetty implements ServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleNetty.class);

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    CommandService commandService;
    CommandUtil commandUtil;

    /**
     * Netty 서버사이드의 ServerBootstrap 의 간단한 구성입니다<br>
     * 연결만을 담당하는 bossGroup 과 I/O 및 연결해제를 담당하는 workerGroup 을 ServerBootstrap 에 등록합니다.<br>
     * InboundHandlerBindUtil 를 생성하고 NettyHandler 에 SimpleNetty (ServiceHandler 의 구현) 을 건네줍니다.<br>
     * InboundHandlerBindUtil 는 ChannelInboundHandlerAdapter 를 확장했고 서버부트스트랩의 채널 파이프라인에 inbound 처리자로 등록되어있기 때문에
     * InboundHandlerBindUtil 는 들어오는 모든 이벤트를 SimpleNetty (ServiceHandler 의 구현) 에게 건네주어 이 안에서 메시지에 대한 직접적인 처리를 하게 됩니다.<br>
     * 이런식으로 요청에 대한 핸들러를 직접적으로 사용하지 않고 인터페이스를 둘러서 오게되면 발생한 이벤트, 수신된 메시지 마다 다른 유형의 ServiceHandler 를 구현하여
     * 하나의 클래스에 모든 처리가 몰리게 되는 상황을 방지 할 수 있고 더욱 유연한 코드 작성이 가능하게 됩니다.
     * @param port
     */

    public void start(int port) {
        commandUtil = new CommandUtil();
        commandUtil.init();
        InboundHandlerBindUtil inboundHandlerBindUtil = new InboundHandlerBindUtil();
        inboundHandlerBindUtil.setServiceHandler(this);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 10);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    ChannelPipeline pipeline = socketChannel.pipeline()
                            .addLast("idleStateHandler", new IdleStateHandler(60, 0, 0))
                            .addLast("messageCodec", new MessageCodec())
                            .addLast("nettyHandler", inboundHandlerBindUtil);
                }
            });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
            logger.info("Lobby Server START ip = {}, port = {}", future.channel().localAddress(), port);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 채널이 연결됐을때 발생하는 메소드<br>
     * 채널이 등록됐을 때와는 다르며, 송수신이 가능한 상태일때 활성화 됩니다.
     * @param channelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        channelGroup.add(channelHandlerContext.channel());

        Message message = Message.create();
        message.init();
        message.setCommand(CommandIDs.LOGIN_ALLOW);

        logger.info("send message | command : {}, size : {}", message.getCommand(), message.getSize());
        channelHandlerContext.writeAndFlush(message);
    }

    /**
     * 채널의 연결이 끊어졌을 경우 발생하는 메소드<br>
     * 연결된 채널을 관리하는 채널 그룹에서 해당 ChannelHandlerContext 를 제거해줍니다.
     * @param channelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        channelGroup.remove(channelHandlerContext.channel());
    }

    /**
     * 메시지가 수신될 때 호출되는 메소드<br>
     * 커맨드 서비스에 수신된 메세지의 커맨드를 꺼내 commandUtil.findFunction() 전달하고 수신된 요청을 처리할 수 있는 클래스를 커맨드서비스에 전달합니다.
     * 이렇게 되면 하나의 commandService 에 commandService 를 상속받은 각기 다른 기능을 하는 객체들을 수신된 요청의 처리에 사용할 수 있는 장점이 있습니다.
     * 그 뒤에 commandService 에 수신된 메세지를 전달하여 필요한 처리를 하게 됩니다.
     * @param channelHandlerContext
     * @param o 사용자가 정의의 메시지 타입을 MessageCodec의 decode에 변환되어 들어오게 되지만 유연성을 위해서 Object 타입으로 전달됩니다.
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) {
        if (o instanceof Message message) {
            message.setChannel(channelHandlerContext.channel());
            commandService = commandUtil.findFunction(message.getCommand());
            commandService.setFunctions(commandUtil.getFunctions());
            commandService.execute(message);
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
            logger.debug("NETTY IO EXCEPTION | {}", cause.getMessage());
            return;
        }
        printStackTrace(cause);
    }

    private void printStackTrace(Throwable throwable) {
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logger.error("NETTY EXCEPTION | {}", stackTraceElement);
        }
    }
}
