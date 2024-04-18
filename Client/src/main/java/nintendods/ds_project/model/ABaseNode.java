package nintendods.ds_project.model;
import jakarta.persistence.Entity;
import java.net.InetAddress;

@Entity
/**
 * An abstract base Node that will be used onto a network interface and has a name, port and address.
 */
public abstract class ABaseNode {
    private InetAddress address; // IP address of the node
    private int port; // Port number of the node
    private String name; // Human-readable name of the node

    // Constructor to initialize the node with an address, port, and name
    public ABaseNode(InetAddress address, int port, String name){
        setAddress(address);
        setPort(port);
        setName(name);
    }

    // Getters and setters for the address, port, and name
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

    // Returns a string representation of the node
    @Override
    public String toString() {
        return "ABaseNode{" +
                "address=" + address +
                ", port=" + port +
                ", name='" + name + '\'' +
                '}';
    }
}
