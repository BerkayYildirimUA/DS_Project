package nintendods.ds_project.model;

import nintendods.ds_project.helper.NameToHash;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class NameServerDatabaseTests {

    @Test
    public void getNodeIDTest_BasicClosest() throws UnknownHostException {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15), address3);

        assertEquals(nameServerDatabase.getNodeID(createStringWithKnownHash(12)), 10);
        assertEquals(nameServerDatabase.getNodeIPfromName(createStringWithKnownHash(12)), address1);
    }

    @Test
    public void getNodeIDTest_WrapFromTop() throws UnknownHostException {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15), address3);

        assertEquals(nameServerDatabase.getNodeID(createStringWithKnownHash(32760)), 10);
        assertEquals(nameServerDatabase.getNodeIPfromName(createStringWithKnownHash(32760)), address1);
    }

    @Test
    public void getNodeIDTest_WrapFromBottom() throws UnknownHostException {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10000), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15000), address3);

        assertEquals(nameServerDatabase.getNodeID(createStringWithKnownHash(100)), 32000);
        assertEquals(nameServerDatabase.getNodeIPfromName(createStringWithKnownHash(100)), address2);
    }

    @Test
    public void getNodeIDTest_ExactMatch() throws UnknownHostException {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");


        nameServerDatabase.addNode(createStringWithKnownHash(10000), address1);
        nameServerDatabase.addNode(createStringWithKnownHash(32000), address2);
        nameServerDatabase.addNode(createStringWithKnownHash(15000), address3);

        assertEquals(nameServerDatabase.getNodeID(createStringWithKnownHash(32000)), 32000);
        assertEquals(nameServerDatabase.getNodeIPfromName(createStringWithKnownHash(100)), address2);
    }


    /**
     * creates random string whos hash after NameToHash.convert is the asked number.
     * @param wantedHash
     * @return
     */
    private String createStringWithKnownHash(int wantedHash){
        Random random = new Random();
        while (true){
            String candidate = random.ints(48, 122).limit(30).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            if (NameToHash.convert(candidate) == wantedHash){
                return candidate;
            }
        }

    }
}
