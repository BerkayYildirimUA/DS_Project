package nintendods.ds_project;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.Node;
import nintendods.ds_project.service.MulticastSender;
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

    static Node node;

    public static void main(String[] args) throws IOException {
        MulticastSender mp = null;
        ServerSocket serverSocket = null;
        try {
            // startup server socket for file transfer
            serverSocket = new ServerSocket(0);

            node = new Node(InetAddress.getLocalHost(), serverSocket, InetAddress.getLocalHost().getHostName());
            mp = new MulticastSender("224.0.0.100", 12345);

            // Een id aan het packet zodat de ontvangers weten (bij verlies van packet) dat bv. 2 dezelfde packets met dezelfde timestamp, identiek zijn.

            long udp_id = System.currentTimeMillis();

            while(true) {
                //send out the node's object
                mp.multicastSend(new MNObject(udp_id, node.getAddress(),node.getPort(), node.getName()));

                //wait for receiving the amount of nodes in the network and the prev and the next node in the network

            }
        } catch (Exception e) {
            serverSocket.close();
            throw new RuntimeException(e);
        }

        //SpringApplication.run(DsProjectApplication.class, args);
    }
    @GetMapping("/")
    public String check() { return "Project is running"; }

}
