package com.rpc.transport.netty.handler;

import com.rpc.core.Request;
import com.rpc.core.Response;
import com.rpc.transport.ProtocolHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Signal;

import java.util.List;

import static com.rpc.transport.ProtocolHeader.*;
/**
 *   消息头12个字节定长
 *   = 2 // MAGIC = (short) 0xbabe
 *   + 1 // 消息标志位, 用来表示消息类型Request/Response/Heartbeat
 *   + 1 // 状态位, 设置请求响应状态
 *   + 4 //扩展属性长度
 *   + 4 // 消息体 body 长度
 */
public class ProtocolDecoder extends ReplayingDecoder<ProtocolDecoder.State> {

    public ProtocolDecoder() {
        super(State.HEADER_MAGIC);
    }

    // 协议头
    private final ProtocolHeader header = new ProtocolHeader();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(in.readShort());         // MAGIC
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                header.sign(in.readByte());         // 消息标志位
                checkpoint(State.HEADER_STATUS);
            case HEADER_STATUS:
                header.status(in.readByte());       // 状态位
                checkpoint(State.HEADER_EXTENSION_ATTRIBUTES_LENGTH);
            case HEADER_EXTENSION_ATTRIBUTES_LENGTH: //扩展属性长度
                header.attrBodyLength(in.readInt());
                checkpoint(State.EXTENSION_ATTRIBUTES_BODY);
            case EXTENSION_ATTRIBUTES_BODY:           //扩展属性消息
                int attrBodyLength = header.attrBodyLength();
                byte[] attrBytes = new byte[attrBodyLength];
                in.readBytes(attrBytes);
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.bodyLength(in.readInt());    // 消息体长度
                checkpoint(State.BODY);
            case BODY:
                switch (header.sign()) {
                    case HEARTBEAT:
                        break;
                    case REQUEST: {
                        int bodyLength = header.bodyLength();
                        byte[] bytes = new byte[bodyLength];
                        in.readBytes(bytes);
                        Request request = new Request();
                        request.bytes(bytes);
                        out.add(request);
                        break;
                    }
                    case RESPONSE: {
                        int bodyLength = header.bodyLength();
                        byte[] bytes = new byte[bodyLength];
                        in.readBytes(bytes);
                        Response response = new Response();
                        response.bytes(bytes);
                        out.add(response);
                        break;
                    }
                }
                checkpoint(State.HEADER_MAGIC);
        }
    }

    private static void checkMagic(short magic) throws Signal {
        if (MAGIC != magic) {
            throw  Signal.valueOf("标志位错误");
        }
    }

    enum State {
        HEADER_MAGIC,
        HEADER_SIGN,
        HEADER_STATUS,
        HEADER_EXTENSION_ATTRIBUTES_LENGTH,
        EXTENSION_ATTRIBUTES_BODY,
        HEADER_BODY_LENGTH,
        BODY
    }
}
