package com.znzlspt.netcore.handler;

import com.znzlspt.netcore.command.ResponseCommand;
import com.znzlspt.netcore.message.CommandDispatcher;
import com.znzlspt.netcore.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component(service = ServiceHandler.class, immediate = true)
public class DefaultServiceHandler implements ServiceHandler{

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceHandler.class);

    @Reference
    private ChannelGroup channelGroup;

    @Reference
    private CommandDispatcher commandDispatcher;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean isRunning = false;

    public DefaultServiceHandler() {
    }

    @Activate
    public void activate() {
        logger.info("DefaultServiceHandler activated with ChannelGroup: {}, CommandDispatcher: {}",
                    channelGroup != null, commandDispatcher != null);
    }

    @Deactivate
    public void deactivate() {
        logger.info("DefaultServiceHandler deactivated");
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
        message.setCommand(ResponseCommand.OK.code());

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
            logger.error("NETTY IO EXCEPTION | {}", cause.getMessage());
            return;
        }
        printStackTrace(cause);
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
        response.setCommand(0);
        response.addString("Unsupported command: " + message.getCommand());

        channel.writeAndFlush(response);
    }
}
