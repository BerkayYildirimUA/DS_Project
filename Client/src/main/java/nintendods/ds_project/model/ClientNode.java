package nintendods.ds_project.model;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientNode extends ABaseNode{
    private int id;
    private int prevId;
    private int nextId;
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

    public int getPrevId() {
        return prevId;
    }

    public void setPrevId(int prevId) {
        this.prevId = prevId;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }
}
