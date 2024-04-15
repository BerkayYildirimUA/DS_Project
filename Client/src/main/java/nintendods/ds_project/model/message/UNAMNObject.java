package nintendods.ds_project.model.message;

/**
 * UNAMN or Unicast from Node After Multicast to Node is a message object that will be sent to
 * the sender node from the node that has transmitted a multicast to join the ring topology.
 * It's used to inform about the position in the ring after a multicast.
 */
public class UNAMNObject extends AMessage {
    private int nodeHashId; // Hash ID of the node this message is being sent to
    private int prevNodeId; // ID of the previous node in the ring
    private int nextNodeId; // ID of the next node in the ring

    /**
     * Constructor with message ID
     * @param messageId The unique message ID
     * @param nodeHashId The node hash ID where we send it to
     * @param prevNodeId The previous node ID (lower than nodeHashId)
     * @param nextNodeId The next node ID (higher than nodeHashId)
     */
    public UNAMNObject(long messageId, eMessageTypes type, int nodeHashId, int prevNodeId, int nextNodeId) {
        super(messageId, type); // Initialize the base class
        setNodeHashId(nodeHashId); // Set the node hash ID
        setPrevNodeId(prevNodeId); // Set the previous node ID
        setNextNodeId(nextNodeId); // Set the next node ID
    }

    /**
     * Constructor without message ID (defaults to 0)
     * @param type
     * @param nodeHashId
     * @param prevNodeId
     * @param nextNodeId
     */
    public UNAMNObject(eMessageTypes type, int nodeHashId, int prevNodeId, int nextNodeId) {
        super(0, type); // Call the superclass constructor with message ID set to 0
        setNodeHashId(nodeHashId); // Set the node hash ID
        setPrevNodeId(prevNodeId); // Set the previous node ID
        setNextNodeId(nextNodeId); // Set the next node ID
    }

    // Getters and setters for nodeHashId, prevNodeId, and nextNodeId
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

    // To string method for debugging purposes
    @Override
    public String toString() {
        return "UNAMObject: " + "\r\n\tprevNode: " + getPrevNodeId() + "\r\n\tcurrNode: " + getNodeHashId()+"\r\n\tnextnode: " + getNextNodeId();
    }
}
