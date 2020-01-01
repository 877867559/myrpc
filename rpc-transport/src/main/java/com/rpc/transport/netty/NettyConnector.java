package com.rpc.transport.netty;

import com.rpc.common.util.ConstantsUtils;
import com.rpc.core.AbstractClient;
import com.rpc.core.model.UnresolvedAddress;
import com.rpc.transport.Connector;
import com.rpc.transport.netty.connector.ConnectorHandler;
import com.rpc.transport.netty.handler.ProtocolDecoder;
import com.rpc.transport.netty.handler.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import static com.rpc.common.util.Preconditions.checkArgument;

public class NettyConnector extends AbstractClient implements Connector {

    private static final int DEFAULT_WORKS = Runtime.getRuntime().availableProcessors();

    private Bootstrap bootstrap;
    private EventLoopGroup worker;
    private final int nWorkers;

    private final ConnectorHandler handler = new ConnectorHandler();

    public NettyConnector(){
        this(DEFAULT_WORKS);
    }

    public NettyConnector(int nWorkers){
        checkArgument(nWorkers > 0);
        this.nWorkers = nWorkers;
        init();
    }

    private void init(){
        bootstrap = new Bootstrap();
        worker = new NioEventLoopGroup(nWorkers);
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sh) throws Exception {
                        sh.pipeline()
                                .addLast(new IdleStateHandler(0, ConstantsUtils.WRITER_IDLE_TIME_SECONDS, 0))
                                .addLast(new ProtocolDecoder()) //解码request
                                .addLast(new ProtocolEncoder()) //编码response
                                .addLast(handler); //使用ServerHandler类来处理接收到的消息
                    }
                });
    }
    @Override
    public  void connect(UnresolvedAddress address) throws InterruptedException{
       bootstrap.connect(address.getHost(), address.getPort()).sync();
    }

    @Override
    public void shutdownGracefully(){
        if(worker != null) worker.shutdownGracefully();
    }
}
