package nintendods.ds_project.model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Node extends ABaseNode{
    int id;
    ServerSocket fileSocket;

    public Node(InetAddress address, int port, String name) {
        super(address, port, name);
    }
    public Node(InetAddress address, ServerSocket socket, String name) throws IOException {
        super(address, socket.getLocalPort(), name);
        this.fileSocket = socket;
    }

    public Node(InetAddress address, int port, String name, int id) {
        super(address, port, name);
        setId(id);
    }

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
}
