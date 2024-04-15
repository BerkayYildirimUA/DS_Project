package nintendods.ds_project.model.message;

/**
 * MN or Multicast Node is a message object that will be sent in a multicast fasion.
 * This is typically used to send a message to multiple nodes at once.
 */
public class MNObject extends AMessage {
    private String address; // Network address of the node sending the message
    private int port; // Network port of the node
    private String name; // Name of the node

    /**
     * Constructor for creating a multicast message object
     * @param messageId The unique message ID
     * @param address The address of the multicaster node
     * @param name The name of the multicaster node
     */
    public MNObject(long messageId, eMessageTypes type, String address,int port, String name) {
        super(messageId, type); // Call the superclass constructor
        setAddress(address); // Set the network address
        setName(name); // Set the name of the node
        setPort(port); // Set the network port
    }

    // Getters and setters for address, name, and port
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

    // To string method to output message details
    public String toString(){
        return getMessageId() + " " + getAddress() + " " + getName();
    }
}
