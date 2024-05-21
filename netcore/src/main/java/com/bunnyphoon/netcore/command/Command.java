package com.bunnyphoon.netcore.command;

import com.bunnyphoon.netcore.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Command {

    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    AtomicLong responseTime = new AtomicLong(0);
    AtomicLong counter = new AtomicLong(0);

    int totalTime = 0;

    Long max = Long.MIN_VALUE;
    Long min = Long.MAX_VALUE;

    final int delayLimit = 200;

    public void setCounter(long value) {
        counter.set(value);
    }

    public void setResponseTime(AtomicLong responseTime) {
        this.responseTime = responseTime;
    }

    public Long getCounter() {
        return counter.get();
    }

    public Long getResponseTime() {
        if (responseTime.get() == 0) {
            return 0L;
        } else {
            return (responseTime.get() / 1000000);
        }
    }

    /**
     * 두개의 추상 메소드는 실제로 사용될 프로젝트에서 구현해야지만 사용이 가능합니다.
     * execute 의 리턴값인 boolean 은 처리시간을 계산할 것인지 구분할 때 사용합니다.
     * @param request
     * @return boolean
     */
    public abstract boolean execute(Message request);

    public abstract String getName();

    public boolean run(Object request) {
        Long startTime = System.nanoTime();
        boolean ret = false;
        ret = execute((Message) request);
        Long endTime = System.nanoTime();

        totalTime = (int) (endTime - startTime) / 1000000;
        if (totalTime > this.delayLimit) {
            logger.warn("Execute slow : {} , {}ms", this.getName(), totalTime);
        }

        counter.incrementAndGet();

        long executeTime = totalTime;

        responseTime.set(responseTime.get() + executeTime);

        if (executeTime < min) min = executeTime;
        if (executeTime > max) max = executeTime;

        return ret;
    }
}
