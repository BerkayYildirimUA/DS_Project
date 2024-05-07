package nintendods.ds_project.labReports;

import nintendods.ds_project.Exeptions.IDTakenExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.utility.NameToHash;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class lab3_Tests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Add a node with a unique node name
    @Test
    public void addNodeWithUniqueName() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, "FIRST NODE");
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, "SECOND NODE");

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());

        assertTrue(nodeDB.exists(node1.getId(), node1.getAddress().getHostAddress()));
        assertTrue(nodeDB.exists(node2.getId(), node2.getAddress().getHostAddress()));
    }

    // Add a node with a unique node name
    @Test(expected = IDTakenExeption.class)
    public void addNodeWithSameName() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, "FIRST NODE");
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, "FIRST NODE");

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());

        fail();
    }

    // Remove a node from the MAP
    @Test
    public void removeNodeFromMAP() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, "FIRST NODE");
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, "SECOND NODE");

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());

        assertTrue(nodeDB.exists(node1.getId(), node1.getAddress().getHostAddress()));
        assertTrue(nodeDB.exists(node2.getId(), node2.getAddress().getHostAddress()));

        nodeDB.deleteNode(node1.getId(), node1.getAddress().getHostAddress());

        assertFalse(nodeDB.exists(node1.getId(), node1.getAddress().getHostAddress()));
        assertTrue(nodeDB.exists(node2.getId(), node2.getAddress().getHostAddress()));

        nodeDB.deleteNode(node2.getId(), node2.getAddress().getHostAddress());

        assertFalse(nodeDB.exists(node1.getId(), node1.getAddress().getHostAddress()));
        assertFalse(nodeDB.exists(node2.getId(), node2.getAddress().getHostAddress()));
    }

    // a filename with a hash smaller than the smallest hash of the nodes
    @Test
    public void filenameWithHashSmallerThanTheSmallestHashOfTheNodes() throws IOException, IDTakenExeption {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, "FIRST NODE");
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, "SECOND NODE");

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());

        int hashLength = Math.min(NameToHash.convert(node1.getName()), NameToHash.convert(node2.getName()));

        String theFile = createStringWithKnownHash(hashLength - 1);

        assertEquals(nodeDB.getClosestIdFromName(theFile), node2.getId());

    }

    private String createStringWithKnownHash(int wantedHash){
        Random random = new Random();
        while (true){
            String candidate = random.ints(0, 1000).limit(30).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            if (NameToHash.convert(candidate) == wantedHash){
                return candidate;
            }
        }
    }
}


