package nintendods.ds_project.model;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ANetworkNodeTest {

    @Test
    public void testConstructorAndGetters() throws UnknownHostException {
        String name = "TestNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;

        ANetworkNode node = new ANetworkNode(address, port, name);

        assertNotNull(node);
        assertEquals(name, node.getName());
        assertEquals(address, node.getAddress());
        assertEquals(port, node.getPort());
    }

    @Test
    public void testSetAddress() throws UnknownHostException {
        InetAddress initialAddress = InetAddress.getByName("127.0.0.1");
        InetAddress newAddress = InetAddress.getByName("192.168.1.1");
        ANetworkNode node = new ANetworkNode(initialAddress, 8080, "TestNode");

        node.setAddress(newAddress);

        assertEquals(newAddress, node.getAddress());
    }

    @Test
    public void testSetPort() throws UnknownHostException {
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int initialPort = 8080;
        int newPort = 9090;
        ANetworkNode node = new ANetworkNode(address, initialPort, "TestNode");

        node.setPort(newPort);

        assertEquals(newPort, node.getPort());
    }

    @Test
    public void testToString() throws UnknownHostException {
        String name = "TestNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        ANetworkNode node = new ANetworkNode(address, port, name);

        String expectedString = "ANetworkNode{address=/127.0.0.1, port=8080, name='TestNode'}";

        assertEquals(expectedString, node.toString());
    }
}
