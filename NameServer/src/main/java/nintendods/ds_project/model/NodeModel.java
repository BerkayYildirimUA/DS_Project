package nintendods.ds_project.model;

import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;

public class NodeModel extends ABaseNode{
    private int id;
    public NodeModel(InetAddress address, int port, String name) {
        super(address, port, name);
        setId(NameToHash.convert(name));
    }
    private void setId(int id) { this.id = id; }
    public int getId() { return id; }
}
