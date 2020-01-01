package com.rpc.transport.netty.handler;

import com.rpc.core.BytesHolder;
import com.rpc.core.Request;
import com.rpc.core.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.rpc.transport.ProtocolHeader.*;


/**
 *   消息头12个字节定长
 *   = 2 // MAGIC = (short) 0xbabe
 *   + 1 // 消息标志位, 用来表示消息类型Request/Response/Heartbeat
 *   + 1 // 状态位, 设置请求响应状态
 *   + 4 //扩展属性长度
 *   + 4 // 消息体 body 长度
 */
public class ProtocolEncoder  extends MessageToByteEncoder<BytesHolder> {

    @Override
    protected void encode(ChannelHandlerContext ctx, BytesHolder msg, ByteBuf out) throws Exception {
        if (msg instanceof Request) {
            doEncodeRequest((Request) msg, out);
        } else if (msg instanceof Response) {
            doEncodeResponse((Response) msg, out);
        } else {
            throw new IllegalArgumentException(msg.toString());
        }
    }

    private void doEncodeRequest(Request request, ByteBuf out) {
        byte[] bytes = request.bytes();
        byte[] attributesBytes = request.attributesBytes();
        out.writeShort(MAGIC)
                .writeByte(REQUEST)
                .writeByte(0x00)
                .writeInt(attributesBytes.length)
                .writeBytes(attributesBytes)
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }

    private void doEncodeResponse(Response response, ByteBuf out) {
        byte[] bytes = response.bytes();
        byte[] attributesBytes = response.attributesBytes();
        out.writeShort(MAGIC)
                .writeByte(RESPONSE)
                .writeByte(SUCSTATUS)
                .writeInt(attributesBytes.length)
                .writeBytes(attributesBytes)
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }
}
