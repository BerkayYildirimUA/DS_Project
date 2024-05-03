package nintendods.ds_project.model;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A specific implementation of a Client node that will be used inside a ring topology.
 */
public class ClientNode extends ABaseNode{
    private int id = -1;
    private int prevNodeId = -1;
    private int nextNodeId = -1;
    public ClientNode(InetAddress address, int port, String name) {
        super(address, port, name);
        setId(NameToHash.convert(name));
    }
    public ClientNode(MNObject multicastNodeObject) throws UnknownHostException {
        super(InetAddress.getByName(multicastNodeObject.getAddress()), multicastNodeObject.getPort(), multicastNodeObject.getName());
        setId(NameToHash.convert(multicastNodeObject.getName()));
    }
    private void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public int getPrevNodeId() {
        return prevNodeId;
    }

    /**
     * Set the previous node ID. If prevNodeId == -1, it gets set to id;
     * @param prevNodeId
     */
    public void setPrevNodeId(int prevNodeId) {
        // if (prevNodeId == -1)
            prevNodeId = getId();
        this.prevNodeId = prevNodeId;
    }

    public int getNextNodeId() {
        return nextNodeId;
    }
    /**
     * Set the next node ID. If nextNodeId == -1, it gets set to id;
     * @param nextNodeId
     */
    public void setNextNodeId(int nextNodeId) {
        if (nextNodeId == -1)
            nextNodeId = getId();
        this.nextNodeId = nextNodeId;
    }

    @Override
    public String toString() {
        return "ClientNode{" +
                "id=" + id +
                ", prevNodeId=" + prevNodeId +
                ", nextNodeId=" + nextNodeId +
                " " + super.toString() +
                '}';
    }
}