package nintendods.ds_project.model;

import java.net.InetAddress;

//@Entity
/**
 * An abstract base Node that will be used onto a network interface and has a name
 */
public class ANetworkNode extends ANode {
    private InetAddress address;
    private int port;

    public ANetworkNode(InetAddress address, int port, String name){
        super(name);
        setAddress(address);
        setPort(port);
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

    @Override
    public String toString() {
        return "ANetworkNode{" +
                "address=" + address +
                ", port=" + port +
                ", name='" + super.getName() + '\'' +
                '}';
    }
}
