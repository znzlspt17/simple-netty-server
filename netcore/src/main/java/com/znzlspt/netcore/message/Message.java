package com.znzlspt.netcore.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.nio.file.attribute.UserPrincipal;


/**
 * 해당 객체는 Netty 에서 네트워크 송수신에사용되는 ByteBuf 클래스를 사용자사 손쉽게 다룰 수 있도록 구현되어 있습니다.
 */

public class Message {

    public final short OK = 1;
    public final short ERROR = 0;

    final static int HEADER_SIZE = 4;
    final static int MAX_BUFFER_SIZE = 1024;

    ByteBuf buffer;
    Channel channel;
    short command = 0;
    short size = 0;

    private Message() {
        buffer = null;
    }

    public static Message create() {
        return new Message();
    }

    public Message init() {
        if (buffer == null) {
            buffer = Unpooled.directBuffer(1024);
            buffer.readerIndex(4);
        }

        return this;
    }

    public byte getByte() {
        return buffer.readByte();
    }

    public Message addByte(byte value) {
        buffer.writeByte(value);
        return this;
    }

    /**
     * ByteBuf에 byte[]로 저장된 문자열을 가져올 때 사용됩니다.
     *
     * @return String
     */

    public String getString() {
        short size = buffer.readShortLE();
        byte[] bytes = new byte[size + 1];
        buffer.readBytes(bytes, 0, size);
        return new String(bytes).trim();
    }

    /**
     * String 을 ByteBuf에 직접적으로 작성할 방법은 제공되지 않으므로 byte[] 로 변환하여 작성한 후
     * 마지막에 문자열의 끝을 직접 넣어줍니다
     *
     * @param value
     * @return 해당 인스턴스
     */
    public Message addString(String value) {
        byte[] bytes = value.getBytes();
        buffer.writeShortLE((short) bytes.length + 1);
        buffer.writeBytes(bytes);
        buffer.writeByte((byte) '\0');

        return this;
    }

    /**
     * 송신할 내용의 작성을 마쳤다면 사이즈를 명시적으로 저장합니다.
     * 접두 set + type 으로 구성된 ByteBuf의 메소드는 readerIndex를 증가시키지 않습니다.
     */
    public void finalizeBuffer() {
        size = (short) (MAX_BUFFER_SIZE - buffer.writableBytes());
        buffer.setShortLE(0, size);
    }

    /**
     * Bytebuf 를 통해 직접 Message를 저장할 경우 사용됩니다.
     * 접두 get + type 으로 구성된 ByteBuf의 메소드는 readerIndex를 증가시키지 않습니다.
     *
     * @param buf
     */
    public void setBuffer(ByteBuf buf) {
        this.buffer = buf;
        this.size = buffer.getShortLE(0);
        this.command = buffer.getShortLE(2);
    }

    /**
     * 메세지를 누가 보냈는지 확인하기 쉽게 통신에 사용된 채널을 저장합니다.
     *
     * @param channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public Object getUser() {
        return (Object) this.channel.attr(AttributeKey.valueOf("user")).get();
    }

    /**
     * 요청자가 원하는 기능의 Command를 저장합니다.
     * 해당 인스턴스를 반환하기 때문에 메소드 체이닝이 가능합니다
     *
     * @param command
     * @return instance
     */
    public Message setCommand(int command) {
        buffer.setShortLE(2, (short) command);
        buffer.writerIndex(4);
        this.command = (short) command;

        return this;
    }

    /**
     * 패킷의 크기를 명시적으로 지정하고 싶은 경우 사용됩니다.
     *
     * @param size
     */
    public void setSize(short size) {
        buffer.setShortLE(0, size);
        this.size = size;
    }

    /**
     * 패킷의 내용물을 간단하게 가져가고 싶은 경우 사용 가능합니다.
     */
    public ByteBuf getBuffer() {
        return buffer.copy();
    }


    public short getCommand() {
        return command;
    }

    public short getSize() {
        return size;
    }

    /**
     * 패킷에 넣고 싶은 내용을 Bytebuf에 대한 상세한 이해 없이 간단하게 도와줍니다.<br>
     * 접두 write + type 으로 구성된 Bytebuf의 메소드는 넣은 type의 사이즈 만큼 writerindex를 증가시킵니다
     * 해당 인스턴스를 반환하기 때문에 메소드 체이닝이 가능합니다.
     *
     * @param value
     * @return 해당 인스턴스
     */

    public Message addBytes(byte[] value) {
        buffer.writeShortLE((short) value.length);
        buffer.writeBytes(value);
        return this;
    }

    public Message addShort(short value) {
        buffer.writeShortLE(value);
        return this;
    }

    public Message addInt(int value) {
        buffer.writeIntLE(value);
        return this;
    }

    public Message addLong(long value) {
        buffer.writeLongLE(value);
        return this;
    }

    public Message addFloat(float value) {
        buffer.writeFloatLE(value);
        return this;
    }

    public Message addDouble(double value) {
        buffer.writeDoubleLE(value);
        return this;
    }

    /**
     * 바이트어레이를 직접 핸들하고 싶을 경우 사용됩니다.
     *
     * @return byte[]
     */
    public byte[] getBytes() {
        short size = buffer.readShort();
        byte[] bytes = new byte[size];
        buffer.readBytes(bytes);
        return bytes;
    }

    /**
     * byteBuf 담긴 내용물을 타입별로 간편하게 가져올 수 있습니다.
     *
     * @return type
     */
    public short getShort() {
        return buffer.readShort();
    }

    public int getInt() {
        return buffer.readInt();
    }

    public long getLong() {
        return buffer.readLong();
    }

    public float getFloat() {
        return buffer.readFloat();
    }

    public double getDouble() {
        return buffer.readDouble();
    }


}
