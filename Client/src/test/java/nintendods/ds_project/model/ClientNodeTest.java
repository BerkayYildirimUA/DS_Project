package nintendods.ds_project.model;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.NameToHash;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static nintendods.ds_project.model.message.eMessageTypes.MulticastNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClientNodeTest {

    @Test
    public void testConstructorAndGetters() throws UnknownHostException {
        String name = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;

        ClientNode node = new ClientNode(address, port, name);

        assertNotNull(node);
        assertEquals(name, node.getName());
        assertEquals(address, node.getAddress());
        assertEquals(port, node.getPort());
        assertEquals(NameToHash.convert(node.getName()), node.getId()); // Id is set later by NameToHash
        assertEquals(-1, node.getPrevNodeId());
        assertEquals(-1, node.getNextNodeId());
    }

    @Test
    public void testConstructorWithMNObject() throws UnknownHostException {
        String name = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        MNObject mnObject = new MNObject(123, MulticastNode, "127.0.0.1", 8080, name);

        ClientNode node = new ClientNode(mnObject);

        assertNotNull(node);
        assertEquals(name, node.getName());
        assertEquals(address, node.getAddress());
        assertEquals(port, node.getPort());
        assertEquals(NameToHash.convert(node.getName()), node.getId());
        assertEquals(-1, node.getPrevNodeId());
        assertEquals(-1, node.getNextNodeId());
    }

    @Test
    public void testSetId() throws UnknownHostException {
        String name = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        ClientNode node = new ClientNode(address, port, name);

        int newId = 123;
        node.setId(newId);

        assertEquals(newId, node.getId());
    }

    @Test
    public void testSetName() throws UnknownHostException {
        String initialName = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        ClientNode node = new ClientNode(address, port, initialName);

        String newName = "NewTestClientNode";
        node.setName(newName);

        assertEquals(newName, node.getName());
    }

    @Test
    public void testSetPrevNodeId() throws UnknownHostException {
        String name = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        ClientNode node = new ClientNode(address, port, name);

        int newPrevNodeId = 123;
        node.setPrevNodeId(newPrevNodeId);

        assertEquals(newPrevNodeId, node.getPrevNodeId());
    }

    @Test
    public void testSetNextNodeId() throws UnknownHostException {
        String name = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        ClientNode node = new ClientNode(address, port, name);

        int newNextNodeId = 456;
        node.setNextNodeId(newNextNodeId);

        assertEquals(newNextNodeId, node.getNextNodeId());
    }

    @Test
    public void testToString() throws UnknownHostException {
        String name = "TestClientNode";
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;
        ClientNode node = new ClientNode(address, port, name);

        String expectedString = "ClientNode{id=" + NameToHash.convert(node.getName()) + ", prevNodeId=-1, nextNodeId=-1 ANetworkNode{address=/127.0.0.1, port=8080, name='TestClientNode'}}";

        assertEquals(expectedString, node.toString());
    }
}
