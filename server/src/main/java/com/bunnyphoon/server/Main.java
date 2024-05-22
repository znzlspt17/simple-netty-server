package com.bunnyphoon.server;

import com.bunnyphoon.server.netty.SimpleNettyRunner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start Simple Netty Server");
        SimpleNettyRunner simpleNetty = new SimpleNettyRunner();
        simpleNetty.start(20999);
    }
}