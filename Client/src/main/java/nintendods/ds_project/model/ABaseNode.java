package nintendods.ds_project.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.net.InetAddress;
import java.net.Socket;

public abstract class ABaseNode {
    private InetAddress address;
    private int port;
    private String name;

    public ABaseNode(InetAddress address, int port, String name){
        setAddress(address);
        setPort(port);
        setName(name);
    }
    
    //alt + insert

    public String getAddress() {
        return address.getHostAddress();
    }

    public InetAddress getAddressFull() {
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
