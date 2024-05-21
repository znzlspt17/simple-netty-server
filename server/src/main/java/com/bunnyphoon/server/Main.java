package com.bunnyphoon.server;

import com.bunnyphoon.server.netty.SimpleNetty;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start Simple Netty Server");
        SimpleNetty simpleNetty = new SimpleNetty();
        simpleNetty.start(20999);
    }
}