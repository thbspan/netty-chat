package org.test.netty.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.test.netty.chat.server.handler.HttpServerHandler;
import org.test.netty.chat.server.handler.WebSocketServerFrameHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private static final int DEFAULT_PORT = 9526;

    private static final String WEBSOCKET_PATH = "/ws";
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("http-codec", new HttpServerCodec())
                                    .addLast("http-aggregator", new HttpObjectAggregator(64 * 1024))
                                    // 处理大文件数据流
                                    .addLast(new ChunkedWriteHandler())
                                    // 处理web页面的请求
                                    .addLast(new HttpServerHandler())
                                    .addLast(new WebSocketServerCompressionHandler())
                                    .addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true))
                                    .addLast(new WebSocketServerFrameHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(DEFAULT_PORT).sync();
            LOGGER.info("chat server successful start");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("chat server start exception", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}
