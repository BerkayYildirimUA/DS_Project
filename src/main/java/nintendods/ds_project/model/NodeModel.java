package nintendods.ds_project.model;

import java.net.InetAddress;

public class NodeModel extends ABaseNode{
    private int id;
    public NodeModel(InetAddress address, int port, int id) {
        super(address, port);
        setId(id);
    }
    private void setId(int id) { this.id = id; }
    public int getId() { return id; }
}
