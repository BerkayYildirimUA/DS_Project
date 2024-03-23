package nintendods.ds_project.model;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.helper.Mapping;
import nintendods.ds_project.helper.NameToHash;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class NameServerDatabaseTests {

    @Test
    public void getNodeIDTest_BasicClosest() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15), address3);

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(12)), 10);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(12)), address1);
    }

    @Test
    public void getNodeIDTest_WrapFromTop() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15), address3);

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(32760)), 10);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(32760)), address1);
    }

    @Test
    public void getNodeIDTest_WrapFromBottom() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10000), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15000), address3);

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(100)), 32000);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(100)), address2);
    }

    @Test
    public void getNodeIDTest_ExactMatch() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10000), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15000), address3);

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(32000)), 32000);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(100)), address2);
    }

    @Test
    public void getNodeIDTest_SameHash() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        System.out.println(nameServerDatabase.addNode(createStringWithKnownHash(10000), address1));
        System.out.println(nameServerDatabase.addNode(createStringWithKnownHash(10000), address2));
        System.out.println(nameServerDatabase.addNode(createStringWithKnownHash(10000), address3));

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(32000)), 10000);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(32000)), address1);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(10001)), address2);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(11000)), address3);
    }

    @Test
    public void getNodeIDTest_SameHash_IDbackTo0() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");

        System.out.println(nameServerDatabase.addNode(createStringWithKnownHash(32768), address1));
        System.out.println(nameServerDatabase.addNode(createStringWithKnownHash(32768), address2));
        System.out.println(nameServerDatabase.addNode(createStringWithKnownHash(32768), address3));

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(32000)), 32768);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(32000)), address1);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(0)), address2);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(1)), address3);
    }

    @Test(expected = NameServerFullExeption.class)
    public void getNodeIDTest_SameHash_FullServer() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();
        InetAddress address = InetAddress.getByName("10.10.10.10");

        // had to fill in the map directly because any other means would be really, really slow
        Field mapField = NameServerDatabase.class.getDeclaredField("nodeID_to_nodeIP");
        mapField.setAccessible(true);
        TreeMap<Integer, InetAddress> nodeID_to_nodeIP = (TreeMap<Integer, InetAddress>) mapField.get(nameServerDatabase);
        for (int i = 0; i <= 32768; i++) {
            nodeID_to_nodeIP.put(i, address);
        }
        mapField.setAccessible(false);

        nameServerDatabase.addNode("ERROR", address);
    }

    @Test
    public void getNodeIDTest_Delete() throws Exception {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15), address3);

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(12)), 10);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(12)), address1);

        nameServerDatabase.deleteNode(address1);

        assertEquals(nameServerDatabase.getClosestNodeID(createStringWithKnownHash(12)), 15);
        assertEquals(nameServerDatabase.getClosestNodeIP(createStringWithKnownHash(12)), address3);
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
