package com.rpc.transport.netty.connector;

import com.rpc.core.Response;
import com.rpc.core.channel.Channel;
import com.rpc.core.consumer.ConsumerProcessor;
import com.rpc.transport.netty.channel.NettyChannel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ConnectorHandler.class);

    private final ConsumerProcessor processor;

    public ConnectorHandler(ConsumerProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Response) {
            Channel jChannel = new NettyChannel(ctx.channel());
            try {
                processor.handleResponse(jChannel, (Response) msg);
            } catch (Throwable t) {
                logger.error("An exception has been caught {}, on {} #channelRead().", t, jChannel);
            }
        } else if (msg instanceof ReferenceCounted){
            ReferenceCountUtil.release(msg);
        }
    }
}
