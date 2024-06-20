package nintendods.ds_project.model.message;

/**
 * UNAMN or Unicast to Node After Multicast to Naming server is a message object that will be sent to
 * the sender node from the node that has transmitted a multicast to join the ring topology.
 */
public class UNAMNObject extends AMessage {
    private int nodeHashId;
    private int prevNodeId;
    private int nextNodeId;

    /**
     * @param messageId The unique message ID
     * @param nodeHashId The node hash ID where we send it to
     * @param prevNodeId The previous node ID (lower than nodeHashId)
     * @param nextNodeId The next node ID (higher than nodeHashId)
     */
    public UNAMNObject(long messageId, eMessageTypes type, int nodeHashId, int prevNodeId, int nextNodeId) {
        super(messageId, type);
        setNodeHashId(nodeHashId);
        setPrevNodeId(prevNodeId);
        setNextNodeId(nextNodeId);
    }

    /**
     * @param type
     * @param nodeHashId
     * @param prevNodeId
     * @param nextNodeId
     */
    public UNAMNObject(eMessageTypes type, int nodeHashId, int prevNodeId, int nextNodeId) {
        super(0, type);
        setNodeHashId(nodeHashId);
        setPrevNodeId(prevNodeId);
        setNextNodeId(nextNodeId);
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

    @Override
    public String toString() {
        return "UNAMObject: " + "\r\n\tprevNode: " + getPrevNodeId() + "\r\n\tcurrNode: " + getNodeHashId()+"\r\n\tnextnode: " + getNextNodeId();
    }
}
