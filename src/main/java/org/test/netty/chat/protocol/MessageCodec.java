package org.test.netty.chat.protocol;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

public class MessageCodec {

    public Message decode(String textMessage) {
        if (StringUtils.isEmpty(textMessage)) {
            return null;
        }

        String[] messages = StringUtils.split(textMessage, '|');
        MessageType messageType = EnumUtils.getEnum(MessageType.class, messages[0]);
        if (messageType == null) {
            return null;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(messages[1]);
        } catch (Exception e) {
            // ignore parse exception
            timestamp = 0;
        }
        String nickName = messages[2];
        Message message;
        switch (messageType) {
            case LOGIN:
                message = new Message(messageType, timestamp, nickName, null);
                break;
            case CHAT:
                message = new Message(messageType, timestamp, nickName, messages[3]);
                break;
            default:
                message = null;
                break;
        }
        return message;
    }

    public String encode(Message message) {
        StringBuilder builder = new StringBuilder();
        MessageType messageType = message.getType();
        builder.append(messageType).append('|')
                .append(System.currentTimeMillis()).append('|');

        if (MessageType.SYSTEM.equals(messageType)) {
            builder.append(message.getCount());
        } else {
            builder.append(message.getSender());
        }
        String content = message.getContent();
        if (StringUtils.isNotEmpty(content)) {
            builder.append('|').append(content);
        }
        return builder.toString();
    }
}
