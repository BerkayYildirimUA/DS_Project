package nintendods.ds_project.model.message;

/**
 * UNAM or Unicast Node After Multicast is a message object that will be sent to
 * the node that has transmitted a multicast to join the ring topology.
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
}
