package nintendods.ds_project.model.message;

/**
 * UNAM or Unicast Node After Multicast is a message object that will be sent to
 * the node that has transmitted a multicast to join the ring topology.
 */
public class UNAMObject extends AMessage {
    private int nodeHashId;
    private int prevNodeId;
    private int nextNodeId;
    private int amountOfNodes;

    /**
     * @param messageId The unique message ID
     * @param nodeHashId The node hash ID where we send it to
     * @param prevNodeId The previous node ID (lower than nodeHashId)
     * @param nextNodeId The next node ID (higher than nodeHashId)
     * @param amountOfNodes The amount of nodes present in the network (exclusive)
     */
    public UNAMObject(long messageId, int nodeHashId, int prevNodeId, int nextNodeId, int amountOfNodes) {
        super(messageId);
        setNodeHashId(nodeHashId);
        setPrevNodeId(prevNodeId);
        setNextNodeId(nextNodeId);
        setAmountOfNodes(amountOfNodes);
    }

    public int getNodeHashId() {
        return nodeHashId;
    }

    public void setNodeHashId(int nodeHashId) {
        this.nodeHashId = nodeHashId;
    }

    public int getPrevNodeId() {
        return prevNodeId;
    }

    public void setPrevNodeId(int prevNodeId) {
        this.prevNodeId = prevNodeId;
    }

    public int getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(int nextNodeId) {
        this.nextNodeId = nextNodeId;
    }

    public int getAmountOfNodes() {
        return amountOfNodes;
    }

    public void setAmountOfNodes(int amountOfNodes) {
        this.amountOfNodes = amountOfNodes;
    }
}
