package com.github.zava.core.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "http")
@RequiredArgsConstructor
public class HttpServer extends ChannelDuplexHandler implements AutoCloseable {
    @Override
    public void close() throws Exception {
        bossGroup.shutdownGracefully();
        workderGroup.shutdownGracefully();
    }

    // 添加 http 相关编码和解码器
    @RequiredArgsConstructor
    public static class HttpChannelInit extends ChannelInitializer<NioServerSocketChannel> {
        private final HttpServer httpServer;
        @Override
        protected void initChannel(NioServerSocketChannel ch) throws Exception {
            log.info("init http channel");
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpRequestDecoder());
            p.addLast(new HttpResponseEncoder());
            p.addLast(httpServer);
        }
    }

    private final int port;

    private ServerBootstrap bootstrap = new ServerBootstrap();
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workderGroup = new NioEventLoopGroup();

    @Sharable
    public static class EchoServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }

    @SneakyThrows
    public void start() {
        bootstrap.group(bossGroup, workderGroup)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new EchoServerHandler());
                }
            });


        ChannelFuture f = bootstrap.bind(port).sync();
        f.channel().closeFuture().sync();
    }

    @SneakyThrows
    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer(6789);
        httpServer.start();
        httpServer.close();
    }
}
