package com.wl4g.component.integration.sharding.api;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import java.util.List;
import java.util.Vector;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.sharding.api.controller.ApiController;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * {@link DispatcherHttpHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-19 v1.0.0
 * @since v1.0.0
 */
public class DispatcherHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final SmartLogger log = getLogger(getClass());

    private final List<Object> controllers = new Vector<>();

    public DispatcherHttpHandler() {
        this.controllers.add(new ApiController());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (null != cause) {
            log.error("", cause);
        }
        if (null != ctx) {
            ctx.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        log.debug("Read message on class: {}", msg.getClass().getName());

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("test".getBytes())); // 2

        HttpHeaders headers = response.headers();
        headers.add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        headers.add(HttpHeaderNames.CONNECTION, "keep-alive");
        headers.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.write(response);
    }

}