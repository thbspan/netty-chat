package org.test.netty.chat.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.test.netty.chat.processor.MessageProcessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketServerFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerFrameHandler.class);

    private final MessageProcessor messageProcessor = new MessageProcessor();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().writeAndFlush(frame.retain()).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof TextWebSocketFrame) {
            messageProcessor.process(ctx.channel(), ((TextWebSocketFrame) frame).text());
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        LOGGER.error("web socket client({}) exception", messageProcessor.getNickName(channel), cause);
        channel.close();
    }
}
