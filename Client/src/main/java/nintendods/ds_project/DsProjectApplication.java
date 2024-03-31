package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.MulticastService;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class DsProjectApplication {

    private static ClientNode node;
    private static final JsonConverter jsonConverter = new JsonConverter();
    private static final int DISCOVERY_RETRIES = 6;

    public static void main(String[] args) throws IOException {
        //Create Node
        node = new ClientNode(InetAddress.getLocalHost(), 21, " Robbe's client");

        eNodeState nodeState = eNodeState.Discovery;
        boolean isRunning = true;
        MulticastService multicastService = null;

        int discoveryRetries = 0;

        while(isRunning) {
            switch (nodeState) {
                case Discovery -> {
                    //Set Discovery on

                    if(discoveryRetries == DISCOVERY_RETRIES){
                        //Max retries reached
                        System.out.println("Max discovery retries reached");
                        nodeState = eNodeState.Error;
                        break;
                    }

                    DiscoveryService ds = new DiscoveryService("224.0.0.100", 12345, 10000);
                    discoveryRetries++;
                    try {
                        node = ds.discover(node);
                    } catch (Exception e) {
                        System.out.println("Retried discovery for the" + discoveryRetries + "(th) time");
                        nodeState = eNodeState.Discovery;
                        break;
                    }

                    System.out.println(node.toString());

                    System.out.println("Successfully reply in " + discoveryRetries + " discoveries.");
                    nodeState = eNodeState.NodeMulticast;
                }
                case NodeMulticast -> {
                    //If first bootup, initialize multicastService
                    if (multicastService == null)
                        multicastService = new MulticastService();
                    //Checks if a multicast has arrived;
                    MNObject message = null;
                    try{ message = multicastService.getMessage();}
                    catch (NullPointerException ignored) {nodeState = eNodeState.Transfer; break;}

                    if(message == null){
                        //No message arrived
                        nodeState = eNodeState.Transfer;
                        break;
                    }
                    else{
                        //Message arrived
                        //compose new node
                        ClientNode newNode = new ClientNode(message);

                        System.out.println("CurrNode: " + node + "\r\n newNode:" + newNode);
                        //Check the position of own node and new node

                        if(     (node.getId() < newNode.getId() && newNode.getId() < node.getNextNodeId()) ||
                                (node.getNextNodeId() == node.getId() && node.getId() < newNode.getId())){
                            //new node is the new next node for current node
                            node.setNextNodeId(newNode.getId());
                            if(node.getPrevNodeId() == node.getId()) node.setPrevNodeId(newNode.getId()); //If 2 nodes are present
                            //Compose message and send out
                            UNAMNObject reply = new UNAMNObject( eMessageTypes.UnicastNodeToNode, node.getId(), -1, newNode.getId() );
                            multicastService.sendReply(reply, newNode);
                        }

                        if(     (node.getPrevNodeId() < newNode.getId() && newNode.getId() < node.getId())||
                                (node.getPrevNodeId() == node.getId() && node.getId() > newNode.getId())){
                            //new node is the new prev node for current node
                            node.setPrevNodeId(newNode.getId());
                            if(node.getNextNodeId() == node.getId()) node.setNextNodeId(newNode.getId()); //If 2 nodes are present
                            //Compose message and send out
                            UNAMNObject reply = new UNAMNObject( eMessageTypes.UnicastNodeToNode, node.getId(), newNode.getId(), -1 );
                            multicastService.sendReply(reply, newNode);
                        }

                    }
                    nodeState = eNodeState.Transfer;
                }
                case Transfer -> {
                    nodeState = eNodeState.NodeMulticast;
                }
                case Shutdown -> {

                }
                case Error -> {
                    //Do something
                    isRunning = false;
                }
                case null, default -> {

                }
            }
        }

        //SpringApplication.run(DsProjectApplication.class, args);
    }
}
