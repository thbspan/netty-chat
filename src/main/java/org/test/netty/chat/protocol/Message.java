package org.test.netty.chat.protocol;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = -1317110528390245570L;
    private MessageType type;
    private long timestamp;
    private int count;
    private String sender;
    private String content;

    public Message() {
    }

    public Message(MessageType type, long timestamp, int count, String content) {
        this.type = type;
        this.timestamp = timestamp;
        this.count = count;
        this.content = content;
    }

    public Message(MessageType type, long timestamp, String sender, String content) {
        this.type = type;
        this.timestamp = timestamp;
        this.sender = sender;
        this.content = content;
    }
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" + "type=" + type +
                ", timestamp='" + timestamp + '\'' +
                ", count=" + count +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
