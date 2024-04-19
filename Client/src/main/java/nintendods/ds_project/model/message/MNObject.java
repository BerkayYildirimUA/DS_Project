package nintendods.ds_project.model.message;

/**
 * MN or Multicast Node is a message object that will be sent in a multicast
 * fasion.
 */
public class MNObject extends AMessage {
    private String address;
    private int port;
    private String name;

    /**
     * @param messageId The unique message ID
     * @param address   The address of the multicaster node
     * @param name      The name of the multicaster node
     */
    public MNObject(long messageId, eMessageTypes type, String address, int port, String name) {
        super(messageId, type);
        setAddress(address);
        setName(name);
        setPort(port);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        return getMessageId() + " " + getAddress() + " " + getName();
    }
}