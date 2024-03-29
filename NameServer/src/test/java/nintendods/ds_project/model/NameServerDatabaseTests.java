package nintendods.ds_project.model;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.utility.NameToHash;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class NameServerDatabaseTests {

    @Test
    public void getNodeIDTest_BasicClosest() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(10));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.13"), 100, createStringWithKnownHash(15));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(12)), 10);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(12)), node1);
    }

    @Test
    public void getNodeIDTest_WrapFromTop() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(32760)), 10);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(32760)), node1);
    }

    @Test
    public void getNodeIDTest_WrapFromBottom() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10000));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15000));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(100)), 32000);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(100)), node2);
    }

    @Test
    public void getNodeIDTest_ExactMatch() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10000));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15000));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(32000)), 32000);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(32000)), node2);
    }

    @Test
    public void getNodeIDTest_SameHash() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10000));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(10000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(10000));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(32000)), 10000);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(32000)), node1);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(10001)), node2);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(11000)), node3);
    }

    @Test
    public void getNodeIDTest_SameHash_IDbackTo0() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(32768));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32768));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(32768));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(32000)), 32768);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(32000)), node1);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(0)), node2);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(1)), node3);
    }

    @Test(expected = NameServerFullExeption.class)
    public void getNodeIDTest_SameHash_FullServer() throws Exception {
        NodeDB nodeDB = new NodeDB();
        InetAddress address = InetAddress.getByName("10.10.10.10");

        // had to fill in the map directly because any other means would be really, really slow
        Field mapField = NodeDB.class.getDeclaredField("nodeID_to_nodeIP");
        mapField.setAccessible(true);
        TreeMap<Integer, ClientNode> nodeID_to_nodeIP = (TreeMap<Integer, ClientNode>) mapField.get(nodeDB);
        for (int i = 0; i <= 32768; i++) {
            nodeID_to_nodeIP.put(i, new ClientNode(address, 100, "stuffing"));
        }
        mapField.setAccessible(false);

        nodeDB.addNode(new ClientNode(address, 100, "ERROR"));
    }

    @Test
    public void getNodeIDTest_Delete() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15));

        nodeDB.addNode(node1);
        nodeDB.addNode(node2);
        nodeDB.addNode(node3);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(12)), 10);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(12)), node1);

        nodeDB.deleteNode(node1);

        assertEquals(nodeDB.getClosestNodeID(createStringWithKnownHash(12)), 15);
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(12)), node3);
    }


    @Test
    public void getNodeIDTest_Exists() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, "name1");

        nodeDB.addNode(node1);

        assertTrue(nodeDB.exists(node1));
        assertEquals(nodeDB.getClosestNode(createStringWithKnownHash(12)), node1);
    }


    /**
     * creates random string whos hash after NameToHash.convert is the asked number.
     * @param wantedHash
     * @return
     */
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
