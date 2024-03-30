package nintendods.ds_project.model.message;

public abstract class AMessage {
    private long messageId;
    private eMessageTypes messageType;

    public AMessage(long messageId, eMessageTypes type) {
        setMessageId(messageId);
        setMessageType(type);
    }

    public long getMessageId() {
        return messageId;
    }

    private void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public eMessageTypes getMessageType() {
        return messageType;
    }

    public void setMessageType(eMessageTypes messageType) {
        this.messageType = messageType;
    }
}
