package nintendods.ds_project.model.message;

public abstract class AMessage {
    private long messageId;

    public AMessage(long messageId) {
        setMessageId(messageId);
    }

    public long getMessageId() {
        return messageId;
    }

    private void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
