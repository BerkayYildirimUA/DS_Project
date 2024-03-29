package nintendods.ds_project.model;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeModel extends ABaseNode{
    private int id;
    public NodeModel(InetAddress address, int port, String name) {
        super(address, port, name);
        setId(NameToHash.convert(name));
    }
    public NodeModel(MNObject multicastNodeObject) throws UnknownHostException {
        super(InetAddress.getByName(multicastNodeObject.getAddress()), multicastNodeObject.getPort(), multicastNodeObject.getName());
        setId(NameToHash.convert(multicastNodeObject.getName()));
    }
    private void setId(int id) { this.id = id; }
    public int getId() { return id; }
}
