package nintendods.ds_project.model.message;

/**
 * UNAM or Unicast to Node After Multicast is a message object that will be by the Naming Server
 * to the sender node.
 * This is typically used to provide a response after a node has sent a multicast message.
 */
public class UNAMObject extends AMessage {
    private int amountOfNodes; // Amount of nodes currently in the network

    /**
     * Constructor for creating a UNAM object
     * @param messageId The unique message ID
     * @param type The type of unicast message defined in {@link eMessageTypes}
     * @param amountOfNodes The amount of nodes present in the network (exclusive)
     */
    public UNAMObject(long messageId, eMessageTypes type, int amountOfNodes) {
        super(messageId, type); // Initialize the base class
        setAmountOfNodes(amountOfNodes); // Set the amount of nodes
    }

    // Getter for amount of nodes
    public int getAmountOfNodes() {
        return amountOfNodes;
    }

    // Setter for amount of nodes
    public void setAmountOfNodes(int amountOfNodes) {
        this.amountOfNodes = amountOfNodes;
    }

    // To string method for debugging purposes
    @Override
    public String toString() {
        return "UNAMObject: " + "\r\n\tamount of nodes: " + getAmountOfNodes();
    }
}
