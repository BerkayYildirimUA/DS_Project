package nintendods.ds_project.model.message;

/**
 * UNAM or Unicast to Node After Multicast is a message object that will be by the Naming Server
 * to the sender node.
 */
public class UNAMObject extends AMessage {
    private int amountOfNodes;

    /**
     * @param messageId The unique message ID
     * @param type The type of unicast message defined in {@link eMessageTypes}
     * @param amountOfNodes The amount of nodes present in the network (exclusive)
     */
    public UNAMObject(long messageId, eMessageTypes type, int amountOfNodes) {
        super(messageId, type);
        setAmountOfNodes(amountOfNodes);
    }

    public int getAmountOfNodes() {
        return amountOfNodes;
    }

    public void setAmountOfNodes(int amountOfNodes) {
        this.amountOfNodes = amountOfNodes;
    }

    @Override
    public String toString() {
        return "UNAMObject: " + "\r\n\tamount of nodes: " + getAmountOfNodes();
    }
}
