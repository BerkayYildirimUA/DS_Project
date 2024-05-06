package nintendods.ds_project.model;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A specific implementation of a Client node that will be used inside a ring
 * topology.
 */
public class ClientNode extends ANetworkNode {
    private int id = -1;
    private int prevNodeId = -1;
    private int nextNodeId = -1;

    public ClientNode(InetAddress address, int port, String name) {
        super(address, port, name);
        setId(NameToHash.convert(name));
    }

    public ClientNode(MNObject multicastNodeObject) throws UnknownHostException {
        super(InetAddress.getByName(multicastNodeObject.getAddress()), multicastNodeObject.getPort(),
                multicastNodeObject.getName());
        setId(NameToHash.convert(multicastNodeObject.getName()));
    }

    void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getPrevNodeId() {

        return prevNodeId;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        setId(NameToHash.convert(name));
    }

    /**
     * Set the previous node ID. If prevNodeId == -1, it gets set to id;
     *
     * @param prevNodeId
     */
    public synchronized void setPrevNodeId(int prevNodeId) {
        if (prevNodeId == -1)
            prevNodeId = getId();
        System.out.println(id + "prev bacame: " + prevNodeId);
        this.prevNodeId = prevNodeId;
    }

    public int getNextNodeId() {
        return nextNodeId;
    }

    /**
     * Set the next node ID. If nextNodeId == -1, it gets set to id;
     *
     * @param nextNodeId
     */
    public synchronized void setNextNodeId(int nextNodeId) {
        if (nextNodeId == -1)
            nextNodeId = getId();
        System.out.println(id + "next bacame: " + nextNodeId);
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