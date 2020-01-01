package com.rpc.transport.netty;

import com.rpc.common.util.ConstantsUtils;
import com.rpc.core.AbstractServer;
import com.rpc.transport.Acceptor;
import com.rpc.transport.netty.acceptor.AcceptorHandler;
import com.rpc.transport.netty.handler.ProtocolDecoder;
import com.rpc.transport.netty.handler.ProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static com.rpc.common.util.Preconditions.*;

public class NettyAcceptor extends AbstractServer implements Acceptor {

    private static final int DEFAULT_PORT = 18820;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final int nWorkers;
    private final int port;

    private AcceptorHandler handler = new AcceptorHandler();

    public NettyAcceptor(){
        this(DEFAULT_PORT);
    }
    public NettyAcceptor(int port){
        this(port,Runtime.getRuntime().availableProcessors());
    }
    public NettyAcceptor(int port,int nWorkers){
        checkArgument(nWorkers > 0);
        this.port = port;
        this.nWorkers = nWorkers;
        init();
    }

    private void init() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(nWorkers);
        bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sh) throws Exception {
                        sh.pipeline()
                                .addLast(new IdleStateHandler(ConstantsUtils.READER_IDLE_TIME_SECONDS, 0, 0))
                                .addLast(new ProtocolDecoder()) //解码request
                                .addLast(new ProtocolEncoder()) //编码response
                                .addLast(handler); //使用ServerHandler类来处理接收到的消息
                    }
                });
    }

    @Override
    public int bindPort(){
        return port;
    }

    @Override
    public void start() throws InterruptedException{
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
        future.channel().closeFuture().sync();
    }
    @Override
    public void shutdownGracefully(){
        if(bossGroup != null) bossGroup.shutdownGracefully();
        if(workerGroup != null) workerGroup.shutdownGracefully();
    }
}
