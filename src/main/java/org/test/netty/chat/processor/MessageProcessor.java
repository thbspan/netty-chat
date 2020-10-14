package org.test.netty.chat.processor;

import org.apache.commons.lang3.StringUtils;
import org.test.netty.chat.protocol.Message;
import org.test.netty.chat.protocol.MessageCodec;
import org.test.netty.chat.protocol.MessageType;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MessageProcessor {
    private static final ChannelGroup ALL_USER_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");

    private final MessageCodec messageCodec = new MessageCodec();

    public void process(Channel client, String msg) {
        Message message = messageCodec.decode(msg);

        if (message == null) {
            return;
        }
        MessageType messageType = message.getType();
        String nickName = client.attr(NICK_NAME).get();
        if (MessageType.LOGIN.equals(messageType) && StringUtils.isEmpty(nickName)) {
            // 首次登录
            nickName = message.getSender();
            client.attr(NICK_NAME).set(nickName);

            ALL_USER_GROUP.add(client);
            Message notifyOthersMessage = new Message(MessageType.SYSTEM, System.currentTimeMillis(), ALL_USER_GROUP.size(), nickName + "已加入群聊");
            ALL_USER_GROUP.writeAndFlush(new TextWebSocketFrame(messageCodec.encode(notifyOthersMessage)), ChannelMatchers.isNot(client));

            Message notifyCurrentUserMessage = new Message(MessageType.SYSTEM, System.currentTimeMillis(), ALL_USER_GROUP.size(), "已与服务器建立连接");
            client.writeAndFlush(new TextWebSocketFrame(messageCodec.encode(notifyCurrentUserMessage)));
        } else if (MessageType.CHAT.equals(messageType)) {
            message.setSender(nickName);
            ALL_USER_GROUP.writeAndFlush(new TextWebSocketFrame(messageCodec.encode(message)));
        }
    }

    public String getNickName(Channel client) {
        return client.attr(NICK_NAME).get();
    }
}
