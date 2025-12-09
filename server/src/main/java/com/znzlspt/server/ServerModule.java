package com.znzlspt.server;

import com.znzlspt.netcore.handler.ServiceHandler;
import com.znzlspt.server.netty.SimpleNettyRunner;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class ServerModule {
    private static final Logger logger = LoggerFactory.getLogger(ServerModule.class);

    private ServiceHandler handler;
    private SimpleNettyRunner runner;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setServiceHandler(ServiceHandler handler) {
        logger.info("ServiceHandler injected: {}", handler);
        this.handler = handler;
    }

    public void unsetServiceHandler(ServiceHandler handler) {
        logger.info("ServiceHandler removed");
        this.handler = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setSimpleNettyRunner(SimpleNettyRunner runner) {
        logger.info("SimpleNettyRunner injected: {}", runner);
        this.runner = runner;
    }

    public void unsetSimpleNettyRunner(SimpleNettyRunner runner) {
        logger.info("SimpleNettyRunner removed");
        this.runner = null;
    }

    @Activate
    public void activate() {
        logger.info("ServerModule activating...");
        logger.info("ServerModule activated - waiting for service injections");
    }

    @Deactivate
    public void deactivate() {
        logger.info("ServerModule deactivating...");
        this.handler = null;
        this.runner = null;
        logger.info("ServerModule deactivated");
    }
}
