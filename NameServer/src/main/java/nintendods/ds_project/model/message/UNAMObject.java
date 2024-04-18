package nintendods.ds_project.model.message;


/**
 * UNAM or Unicast to Node After Multicast is a message object that will be by the Naming Server
 * to the sender node.
 */
public class UNAMObject extends AMessage {
    private int amountOfNodes;
    private int namingServerPort;
    private String namingServerAddress;

    /**
     * @param messageId The unique message ID
     * @param type The type of unicast message defined in {@link eMessageTypes}
     * @param amountOfNodes The amount of nodes present in the network (exclusive)
     * @param nsAddress The address of the naming server API
     * @param nsPort The port of the namingserver API
     */
    public UNAMObject(long messageId, eMessageTypes type, int amountOfNodes, String nsAddress, int nsPort) {
        super(messageId, type);
        setAmountOfNodes(amountOfNodes);
        setNSAddress(nsAddress);
        setNSPort(nsPort);
    }

    public int getAmountOfNodes() {
        return amountOfNodes;
    }

    public void setAmountOfNodes(int amountOfNodes) {
        this.amountOfNodes = amountOfNodes;
    }

    public String getNSAddress() {
        return namingServerAddress;
    }

    public void setNSAddress(String nsAddress) {
        this.namingServerAddress = nsAddress;
    }

    public int getNSPort() {
        return namingServerPort;
    }

    public void setNSPort(int nsPort) {
        this.namingServerPort = nsPort;
    }

    @Override
    public String toString() {
        return "UNAMObject: " + "\r\n\tamount of nodes: " + getAmountOfNodes();
    }
}