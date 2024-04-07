package nintendods.ds_project.model;
import jakarta.persistence.Entity;
import java.net.InetAddress;

@Entity
/**
 * An abstract base Node that will be used onto a network interface and has a name
 */
public abstract class ABaseNode {
    private InetAddress address;
    private int port;
    private String name;

    public ABaseNode(InetAddress address, int port, String name){
        setAddress(address);
        setPort(port);
        setName(name);
    }
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ABaseNode{" +
                "address=" + address +
                ", port=" + port +
                ", name='" + name + '\'' +
                '}';
    }
}
