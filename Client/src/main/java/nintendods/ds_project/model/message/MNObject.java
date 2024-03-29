package nintendods.ds_project.model.message;

/**
 * MN or Multicast Node is a message object that will be sent in a multicast fasion.
 */
public class MNObject extends AMessage {
    private String address;
    private String name;

    /**
     * @param messageId The unique message ID
     * @param address The address of the multicaster node
     * @param name The name of the multicaster node
     */
    public MNObject(long messageId, String address, String name) {
        super(messageId);
        setAddress(address);
        setName(name);
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
}
