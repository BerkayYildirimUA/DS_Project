package nintendods.ds_project.model;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A specific implementation of a Client node that will be used inside a ring
 * topology.
 * It inherits from ABaseNode and includes ring topology specific properties.
 */
public class ClientNode extends ABaseNode {
    private int id = -1; // Unique identifier for the node within the ring
    private int prevNodeId = -1; // Identifier for the previous node in the ring
    private int nextNodeId = -1; // Identifier for the next node in the ring

    // Constructor initializing a client node with address, port, and name
    public ClientNode(InetAddress address, int port, String name) {
        super(address, port, name); // Call to superclass constructor
        setId(NameToHash.convert(name)); // Set the unique identifier using a hash function
    }

    // Alternative constructor that initializes the client node from an MNObject (multicast node object)
    public ClientNode(MNObject multicastNodeObject) throws UnknownHostException {
        super(InetAddress.getByName(multicastNodeObject.getAddress()), multicastNodeObject.getPort(),
                multicastNodeObject.getName());
        setId(NameToHash.convert(multicastNodeObject.getName()));
    }

    // Setter for node ID
    private void setId(int id) {
        this.id = id;
    }

    // Getter for node ID
    public int getId() {
        return id;
    }

    // Getter and setter for previous node ID
    public int getPrevNodeId() {
        return prevNodeId;
    }

    /**
     * Set the previous node ID. If prevNodeId == -1, it gets set to id;
     * 
     * @param prevNodeId
     */
    public void setPrevNodeId(int prevNodeId) {
        if (prevNodeId == -1)
            prevNodeId = getId();
        this.prevNodeId = prevNodeId;
    }

    // Getter and setter for next node ID
    public int getNextNodeId() {
        return nextNodeId;
    }

    /**
     * Set the next node ID. If nextNodeId == -1, it gets set to id;
     * 
     * @param nextNodeId
     */
    public void setNextNodeId(int nextNodeId) {
        if (nextNodeId == -1)
            nextNodeId = getId();
        this.nextNodeId = nextNodeId;
    }

    // Returns a detailed string representation of the client node
    @Override
    public String toString() {
        return "\r\nname: " + getName() + "\r\nprevious node:\t" + getPrevNodeId() + "\r\nid:\t\t" + getId() + "\r\nnextNode:\t" + getNextNodeId() + "\r\n";
    }
}