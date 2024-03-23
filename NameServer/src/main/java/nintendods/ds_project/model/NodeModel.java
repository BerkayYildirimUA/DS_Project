package nintendods.ds_project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;

public class NodeModel extends ABaseNode{
    private int id;

    public NodeModel(InetAddress address, int port) {
        super(address, port);
    }

    private void setId(int id) { this.id = id; }
    public int getId() { return id; }

    @JsonCreator
    public NodeModel(@JsonProperty("address") InetAddress address,
                     @JsonProperty("port") int port,
                     @JsonProperty("id") int id) {
        super(address, port);
        setId(id);
    }
}
