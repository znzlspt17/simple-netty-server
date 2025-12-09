package com.znzlspt.netcore.handler;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChannelGroup을 OSGi 서비스로 제공하는 컴포넌트입니다.
 */
@Component(service = ChannelGroup.class, immediate = true)
public class ChannelGroupProvider extends DefaultChannelGroup {
    private static final Logger logger = LoggerFactory.getLogger(ChannelGroupProvider.class);

    public ChannelGroupProvider() {
        super(GlobalEventExecutor.INSTANCE);
    }

    @Activate
    public void activate() {
        logger.info("ChannelGroup service activated");
    }

    @Deactivate
    public void deactivate() {
        logger.info("ChannelGroup service deactivating - closing all channels");
        this.close().awaitUninterruptibly();
        logger.info("ChannelGroup service deactivated");
    }
}
