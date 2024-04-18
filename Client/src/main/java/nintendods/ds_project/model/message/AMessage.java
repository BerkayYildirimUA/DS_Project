package nintendods.ds_project.model.message;

// Abstract class AMessage defines the basic structure of a message object in the system.
public abstract class AMessage {
    private long messageId; // Unique identifier for each message
    private eMessageTypes messageType; // Enum that defines the type of message

    // Constructor to initialize a new message with its ID and type
    public AMessage(long messageId, eMessageTypes type) {
        setMessageId(messageId);
        setMessageType(type);
    }

    // Getter for message ID
    public long getMessageId() {
        return messageId;
    }

    // Setter for message ID
    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    // Getter for message type
    public eMessageTypes getMessageType() {
        return messageType;
    }

    // Setter for message type
    public void setMessageType(eMessageTypes messageType) {
        this.messageType = messageType;
    }
}
