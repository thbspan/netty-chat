package org.test.netty.chat.server.handler;

import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerHandler.class);

    private static final Pattern PATTERN_END_WITH_IMAGES = Pattern.compile(".*\\.(jpg|png|gif)$");

    public static final String WEB_ROOT_PATH = "/static";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();

        String page = WEB_ROOT_PATH + ("/".equals(uri) ? "/index.html" : uri);

        try (RandomAccessFile file = new RandomAccessFile(
                Paths.get(HttpServerHandler.class.getResource(page).toURI()).toFile(), "r")) {

            String contextType;
            if (uri.endsWith(".css")) {
                contextType = "text/html";
            } else if (uri.endsWith(".js")) {
                contextType = "text/javascript";
            } else if (PATTERN_END_WITH_IMAGES.matcher(uri).matches()) {
                contextType = "image/" + uri.substring(uri.lastIndexOf("."));
            } else if (uri.endsWith(".ico")) {
                contextType = "image/x-icon";
            } else {
                contextType = "text/html";
            }
            // 设置返回消息内容
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contextType + ";charset=utf-8;");
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            HttpUtil.setKeepAlive(response, keepAlive);
            HttpUtil.setContentLength(response, file.length());

            ctx.write(response);
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            LOGGER.error("read page({}) exception", page, e);
            ctx.fireChannelRead(request.retain());
        }

    }
}
