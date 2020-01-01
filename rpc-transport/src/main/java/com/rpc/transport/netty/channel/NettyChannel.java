package com.rpc.transport.netty.channel;

import com.rpc.core.channel.Channel;
import com.rpc.core.channel.ChannelListener;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.SocketAddress;

public class NettyChannel implements Channel {

    private final io.netty.channel.Channel channel;

    public NettyChannel(io.netty.channel.Channel channel){
        this.channel = channel;
    }
    public io.netty.channel.Channel channel() {
        return channel;
    }

    @Override
    public String id() {
        return channel.id().asShortText(); // 注意这里的id并不是全局唯一, 单节点中是唯一的
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }


    @Override
    public SocketAddress localAddress() {
        return channel.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public Channel close() {
        channel.close();
        return this;
    }


    @Override
    public Channel write(Object msg) {
        channel.writeAndFlush(msg, channel.voidPromise());
        return this;
    }

    @Override
    public Channel write(Object msg, final ChannelListener<Channel> listener) {
        final Channel jChannel = this;
        channel.writeAndFlush(msg)
                .addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            listener.operationSuccess(jChannel);
                        } else {
                            listener.operationFailure(jChannel, future.cause());
                        }
                    }
                });
        return jChannel;
    }
}
