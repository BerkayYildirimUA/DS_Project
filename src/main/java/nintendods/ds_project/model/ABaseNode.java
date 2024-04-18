package nintendods.ds_project.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.net.InetAddress;
import java.net.Socket;

public abstract class ABaseNode {
    private InetAddress address;
    private int port;

    public ABaseNode(InetAddress address, int port){
        setAddress(address);
        setPort(port);
    }
    
    //alt + insert

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
