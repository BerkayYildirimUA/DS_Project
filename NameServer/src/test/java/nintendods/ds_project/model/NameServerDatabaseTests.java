package nintendods.ds_project.model;

import nintendods.ds_project.Exeptions.IDTakenExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.utility.NameToHash;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NameServerDatabaseTests {

    @Test
    public void getNodeIDTest_BasicClosest() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(10));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.13"), 100, createStringWithKnownHash(15));

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());
        nodeDB.addNode(node3.getName(), node3.getAddress().getHostAddress());

        assertEquals(10, nodeDB.getClosestIdFromName(createStringWithKnownHash(12)));
        assertEquals(node1.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(12)));
    }

    @Test
    public void getNodeIDTest_WrapFromTop() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15));

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());
        nodeDB.addNode(node3.getName(), node3.getAddress().getHostAddress());

        assertEquals(10, nodeDB.getClosestIdFromName(createStringWithKnownHash(32760)));
        assertEquals(node1.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(32760)));
    }

    @Test
    public void getNodeIDTest_WrapFromBottom() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10000));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15000));

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());
        nodeDB.addNode(node3.getName(), node3.getAddress().getHostAddress());

        assertEquals(32000, nodeDB.getClosestIdFromName(createStringWithKnownHash(100)));
        assertEquals(node2.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(100)));
    }

    @Test
    public void getNodeIDTest_ExactMatch() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10000));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15000));

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());
        nodeDB.addNode(node3.getName(), node3.getAddress().getHostAddress());

        assertEquals(32000, nodeDB.getClosestIdFromName(createStringWithKnownHash(32000)));
        assertEquals(node2.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(32000)));
    }

    @Test(expected = IDTakenExeption.class)
    public void getNodeIDTest_SameHash() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10000));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(10000));

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());

       fail();
    }

    @Test
    public void getNodeIDTest_Delete() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, createStringWithKnownHash(10));
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, createStringWithKnownHash(32000));
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, createStringWithKnownHash(15));

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());
        nodeDB.addNode(node3.getName(), node3.getAddress().getHostAddress());

        assertEquals(10, nodeDB.getClosestIdFromName(createStringWithKnownHash(12)));
        assertEquals(node1.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(12)));

        nodeDB.deleteNode(node1.getAddress().getHostAddress());

        assertEquals(15, nodeDB.getClosestIdFromName(createStringWithKnownHash(12)));
        assertEquals(node3.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(12)));
    }


    @Test
    public void BasicExistsTest() throws Exception {
        NodeDB nodeDB = new NodeDB();

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, "name1");

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());

        assertTrue(nodeDB.exists(node1.getAddress().getHostAddress()));
        assertEquals(node1.getAddress().getHostAddress(), nodeDB.getClosestIpFromName(createStringWithKnownHash(12)));
    }

    @Test
    public void SameHashExistsTest() throws Exception {
        NodeDB nodeDB = new NodeDB();

        String nameServer1 = createStringWithKnownHash(10);
        String nameServer2;
        do {
            nameServer2 = createStringWithKnownHash(10);
        } while (nameServer1.equals(nameServer2)); // this is to make sure they have diffrent names

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, nameServer1);
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 10, nameServer2);

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());

        assertFalse(nodeDB.exists(node2.getAddress().getHostAddress()));
        assertTrue(nodeDB.exists(node1.getAddress().getHostAddress()));
    }

    @Test
    public void saveDBTest() throws Exception {
        NodeDB nodeDB = new NodeDB();
        Random random = new Random();

        String name1;
        String name2;
        String name3;

        do {
            name1 = createStringWithKnownHash(random.nextInt(NameToHash.MAX_NODES - 1));
            name2 = createStringWithKnownHash(random.nextInt(NameToHash.MAX_NODES - 1));
            name3 = createStringWithKnownHash(random.nextInt(NameToHash.MAX_NODES - 1));
        }while (name1.equals(name2) || name1.equals(name3) || name2.equals(name3));

        ClientNode node1 = new ClientNode(InetAddress.getByName("10.10.10.10"), 100, name1);
        ClientNode node2 = new ClientNode(InetAddress.getByName("10.10.10.11"), 100, name2);
        ClientNode node3 = new ClientNode(InetAddress.getByName("10.10.10.12"), 100, name3);

        nodeDB.addNode(node1.getName(), node1.getAddress().getHostAddress());
        nodeDB.addNode(node2.getName(), node2.getAddress().getHostAddress());
        nodeDB.addNode(node3.getName(), node3.getAddress().getHostAddress());

        nodeDB.saveDB();

        NodeDB nodeDB2 = new NodeDB();
        nodeDB2.loadDB();

        assertEquals(nodeDB2.getClosestIdFromName(name1), node1.getId());
        assertEquals(nodeDB2.getClosestIdFromName(name2), node2.getId());
        assertEquals(nodeDB2.getClosestIdFromName(name3), node3.getId());

        assertEquals(nodeDB2.getClosestIpFromName(name1), node1.getAddress().getHostAddress());
        assertEquals(nodeDB2.getClosestIpFromName(name2), node2.getAddress().getHostAddress());
        assertEquals(nodeDB2.getClosestIpFromName(name3), node3.getAddress().getHostAddress());
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
