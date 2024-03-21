package nintendods.ds_project.model;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootTest
public class NameServerDatabaseTests {

    @Test
    public void closedTest() throws UnknownHostException {
        NameServerDatabase nameServerDatabase = new NameServerDatabase();

        InetAddress address1 = InetAddress.getByName("10.10.10.10");
        InetAddress address2 = InetAddress.getByName("10.10.10.11");
        InetAddress address3 = InetAddress.getByName("10.10.10.12");
        InetAddress address4 = InetAddress.getByName("10.10.10.13");
        InetAddress address5 = InetAddress.getByName("10.10.10.14");

        nameServerDatabase.addNode("Jeff", address1);
        nameServerDatabase.addNode("Noodle", address2);
        nameServerDatabase.addNode("Sefob", address3);
        nameServerDatabase.addNode("qodif", address4);
        nameServerDatabase.addNode("LOL", address5);

        System.out.println(nameServerDatabase.getNodeID("echteFile.text"));
        System.out.println("echteFile.text".hashCode());

    }
}
