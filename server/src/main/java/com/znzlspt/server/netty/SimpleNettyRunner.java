package com.znzlspt.server.netty;

import com.znzlspt.netcore.handler.InboundHandlerBindHelper;
import com.znzlspt.netcore.handler.ServiceHandler;
import com.znzlspt.netcore.message.Codec;
import com.znzlspt.server.service.CommandRegistry;
import com.znzlspt.server.service.DefaultCommandDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(immediate = true, property = {"port=29000"})
public class SimpleNettyRunner {

    private static final Logger logger = LoggerFactory.getLogger(SimpleNettyRunner.class);

    @Reference
    private ServiceHandler injectedHandler;
    @Reference
    private InboundHandlerBindHelper injectedInboundHelper;
    @Reference
    private DefaultCommandDispatcher defaultCommandDispatcher;
    @Reference
    private CommandRegistry commandRegistry;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @Activate
    public void start(Map<String, Object> props) {
        int port = 29000;
        Object p = props.get("port");
        if (p != null) {
            try {
                port = Integer.parseInt(String.valueOf(p));
            } catch (NumberFormatException ignored) {
            }
        }

        final int finalPort = port;

        // Netty 서버를 별도 스레드에서 실행하여 블로킹 방지
        Thread serverThread = new Thread(() -> {
            bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();

                serverBootstrap.group(bossGroup, workerGroup);
                serverBootstrap.channel(NioServerSocketChannel.class);
                serverBootstrap.option(ChannelOption.SO_BACKLOG, 10);
                serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast("idleStateHandler", new IdleStateHandler(60, 0, 0))
                                .addLast("Codec", new Codec())
                                .addLast("nettyHandler", injectedInboundHelper);
                    }
                });

                ChannelFuture future = serverBootstrap.bind(finalPort).sync();
                serverChannel = future.channel();
                logger.info("Netty Server started on port {}", finalPort);

                // 서버 채널이 닫힐 때까지 대기
                serverChannel.closeFuture().sync();
                logger.info("Netty Server stopped");

            } catch (InterruptedException e) {
                logger.warn("Netty Server interrupted", e);
                Thread.currentThread().interrupt();
            } finally {
                if (bossGroup != null) {
                    bossGroup.shutdownGracefully();
                }
                if (workerGroup != null) {
                    workerGroup.shutdownGracefully();
                }
            }
        }, "Netty-Server-Thread");

        serverThread.setDaemon(false);
        serverThread.start();

        logger.info("SimpleNettyRunner activated - server starting on port {}", port);
    }

    @Deactivate
    void deactivate() {
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
                serverChannel = null;
            }
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                bossGroup = null;
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                workerGroup = null;
            }
        }

        logger.info("SimpleNettyRunner stopped");
    }
        /**
         * Netty 서버사이드의 ServerBootstrap 의 간단한 구성입니다<br>
         * 연결만을 담당하는 bossGroup 과 I/O 및 연결해제를 담당하는 workerGroup 을 ServerBootstrap 에 등록합니다.<br>
         * InboundHandlerBindHelper 를 생성하고 NettyHandler 에 SimpleNetty (ServiceHandler 의 구현) 을 건네줍니다.<br>
         * InboundHandlerBindHelper 는 ChannelInboundHandlerAdapter 를 확장했고 서버부트스트랩의 채널 파이프라인에 inbound 처리자로 등록되어있기 때문에
         * InboundHandlerBindHelper 는 들어오는 모든 이벤트를 SimpleNetty (ServiceHandler 의 구현) 에게 건네주어 이 안에서 메시지에 대한 직접적인 처리를 하게 됩니다.<br>
         * 이런식으로 요청에 대한 핸들러를 직접적으로 사용하지 않고 인터페이스를 경유하여 오게되면 발생한 이벤트, 수신된 메시지 마다 다른 유형의 ServiceHandler 를 구현하여
         * 하나의 클래스에 모든 처리가 몰리게 되는 상황을 방지 할 수 있고 더욱 유연한 코드 작성이 가능하게 됩니다.
         * @param port
         */

}