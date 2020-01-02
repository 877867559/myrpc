package com.rpc.transport.netty.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.Timer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ChannelHandler.Sharable
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask {

    private Timer timer;
    private Bootstrap bootstrap;
    private final int port;
    private final String host;
    private volatile boolean reconnect = true;
    private int attempts;
    private ChannelHandler[] handlers;

    public ConnectionWatchdog(Bootstrap bootstrap,String host,int port,ChannelHandler[] handlers){
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.handlers = handlers;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (reconnect) {
            if (attempts < 5) {
                attempts++;
            }
            long timeout = 2 << attempts;
            timer.newTimeout(this, timeout, MILLISECONDS);
        }
        ctx.fireChannelInactive();
    }
    @Override
    public void run(Timeout timeout) throws Exception{
        ChannelFuture future;
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handlers);
                }
            });
            future = bootstrap.connect(host,port);
        }
        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();
                if (!succeed) {
                    f.channel().pipeline().fireChannelInactive();
                }
            }
        });
    }

}
