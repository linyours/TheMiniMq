package com.linyous.mqtt.server.handler;

import com.linyous.mqtt.server.common.Cache;
import com.linyous.mqtt.server.common.Status;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
import java.util.Map;

/**
 * @author Linyous
 * @date 2021/6/20 11:44
 */
public class ExceptionHandler extends ChannelInboundHandlerAdapter implements BaseHandlerInterface {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        dealException(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        dealException(ctx);
    }

    private void dealException(ChannelHandlerContext ctx) {
        String ipAddress = getIpAddress(ctx);
        if (Cache.CLIENT_MAP.containsKey(ipAddress)) {
            if (Cache.IDENT_IP_MAP.containsKey(Cache.CLIENT_MAP.get(ipAddress).getClientIdentifier())) {
                Cache.IDENT_IP_MAP.remove(Cache.CLIENT_MAP.get(ipAddress).getClientIdentifier());
            }
            Cache.CLIENT_MAP.remove(ipAddress);
        }
        Status.CONN_COUNT.getAndDecrement();
        for (Map.Entry<String, List<String>> stringListEntry : Cache.TOPIC_MAP.entrySet()) {
            if (stringListEntry.getValue().contains(ipAddress)) {
                stringListEntry.getValue().remove(ipAddress);
            }
        }
    }
}
