package com.github.zava.core.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public abstract class AbstractWsClient extends SimpleChannelInboundHandler<Object> implements AutoCloseable {
    protected String uri;
    protected EventLoopGroup group;
    protected Channel ch;
    protected WebSocketClientHandshaker handshaker;
    protected ChannelPromise handshakeFuture;
    protected int maxContentSize = 1024 * 1024 * 64;

    public void onFrame(WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame textFrame) {
            onText(textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            log.debug("pong received");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.debug("WebSocket Client received closing");
            ch.close();
        }
    }

    public abstract void onClose();

    public abstract void onText(String text);


    @SneakyThrows
    public void connect() {
        URI uri = new URI(this.uri);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new RuntimeException("Only WS(S) is supported.");
        }

        final SslContext sslCtx = "wss".equalsIgnoreCase(scheme) ?
            SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build() : null;

        group = new NioEventLoopGroup();
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, true,
            new DefaultHttpHeaders(), maxContentSize
        );
        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.

        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                    }
                    p.addLast(
                        new HttpClientCodec(),
                        new HttpObjectAggregator(maxContentSize),
                        WebSocketClientCompressionHandler.INSTANCE,
                        this);
                }
            });

        ch = b.connect(uri.getHost(), port).sync().channel();
        handshakeFuture.sync();
    }

    public void send(String msg) {
        WebSocketFrame frame = new TextWebSocketFrame(msg);
        ch.writeAndFlush(frame);
    }

    public void ping() {
        WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
        ch.writeAndFlush(frame);
    }

    @Override
    @SneakyThrows
    public void close() {
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        group.shutdownGracefully();
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("WebSocket Client disconnected!");
        onClose();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                log.debug("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log.error("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                "Unexpected FullHttpResponse (getStatus=" + response.status() +
                    ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        onFrame(frame);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exception caught close connection");
        log.error("", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
