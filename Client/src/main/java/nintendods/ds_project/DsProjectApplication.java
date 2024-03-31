package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;

import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.MulticastService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@RestController
public class DsProjectApplication {

    private static ClientNode node;

    public static void main(String[] args) throws IOException {
        //Create Node
        node = new ClientNode(InetAddress.getLocalHost(), 21, "Robbe's client 1");

        //Set Discovery on
        DiscoveryService ds = new DiscoveryService("224.0.0.100", 12345, 10000);

        try {
            node = ds.discover(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println(node.toString());

        //MulticastService mp = null;
        //ServerSocket serverSocket = null;
        //try {
        //    // startup server socket for file transfer
        //    serverSocket = new ServerSocket(0);
//
        //    node = new ClientNode(InetAddress.getLocalHost(), serverSocket.getLocalPort(), InetAddress.getLocalHost().getHostName());
        //    mp = new MulticastService("224.0.0.100", 12345);
//
        //    // Een id aan het packet zodat de ontvangers weten (bij verlies van packet) dat bv. 2 dezelfde packets met dezelfde timestamp, identiek zijn.
//
        //    long udp_id = System.currentTimeMillis();
//
        //    while(true) {
        //        //send out the node's object
        //        mp.multicastSend(new MNObject(udp_id, node.getAddress(),node.getPort(), node.getName()));
//
        //        //wait for receiving the amount of nodes in the network and the prev and the next node in the network
//
        //    }
        //} catch (Exception e) {
        //    serverSocket.close();
        //    throw new RuntimeException(e);
        //}

        //SpringApplication.run(DsProjectApplication.class, args);
    }
    @GetMapping("/")
    public String check() { return "Project is running"; }

}
