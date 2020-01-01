package com.rpc.transport.netty.acceptor;


import com.rpc.core.Request;
import com.rpc.core.channel.Channel;
import com.rpc.core.processor.ProviderProcessor;
import com.rpc.transport.netty.channel.NettyChannel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class AcceptorHandler  extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AcceptorHandler.class);

    private final ProviderProcessor processor;

    public AcceptorHandler(ProviderProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Request) {
            Channel jChannel = new NettyChannel(ctx.channel());
            Request request = (Request) msg;
            try {
                processor.handleRequest(jChannel, request);
            } catch (Throwable t) {
                logger.error("An exception has been caught {}, on {} #channelRead().", t, jChannel);
            }
        } else if (msg instanceof ReferenceCounted){
            ReferenceCountUtil.release(msg);
        }
    }
}
