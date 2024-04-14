package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.MulticastService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.io.IOException;
import java.net.InetAddress;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class DsProjectApplication {

    private static ClientNode node;
    private static final int DISCOVERY_RETRIES = 6;

    public static void main(String[] args) throws IOException {
        //Create Node
        
        node = new ClientNode(InetAddress.getLocalHost(), 21, generateRandomString(20));
        System.out.println("New node with name: " + node.getName()+ " And hash: " + node.getId());

        eNodeState nodeState = eNodeState.Discovery;
        boolean isRunning = true;
        MulticastService multicastService = null;

        int discoveryRetries = 0;

        while(isRunning) {
            //Finite state machine with eNodeState states
            switch (nodeState) {
                case Discovery -> {
                    //Set Discovery on

                    if(discoveryRetries == DISCOVERY_RETRIES){
                        //Max retries reached
                        System.out.println("Max discovery retries reached");
                        nodeState = eNodeState.Error;
                        break;
                    }

                    DiscoveryService ds = new DiscoveryService("224.0.0.100", 12345, 2000);
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
                    nodeState = eNodeState.Listening;
                }
                case Listening -> {
                    //If first bootup, initialize multicastService
                    if (multicastService == null)
                        multicastService = new MulticastService();
                    //Checks if a multicast has arrived;
                    MNObject message = null;
                    try{ message = multicastService.getMessage();}
                    catch (NullPointerException ignored) {nodeState = eNodeState.Transfer; break;}

                    if(message != null){
                        //Message arrived
                        //compose new node if needed
                        boolean send = false;
                        ClientNode incommingNode = new ClientNode(message);

                        //System.out.println(incommingNode);
                        //Check the position of own node and incomming node
                        if(node.getId() < incommingNode.getId() && (incommingNode.getId() <= node.getNextNodeId() || node.getNextNodeId() == node.getId())){
                            //new node is the new next node for current node
                            node.setNextNodeId(incommingNode.getId());
                            //Check if first node of network?
                            if(node.getId() == node.getPrevNodeId())
                                node.setPrevNodeId(incommingNode.getId());    
                            System.out.println("\r\n current node is below the incomming node\r\n");
                            send = true;
                        }

                        if(node.getId() > incommingNode.getId() && (incommingNode.getId() >= node.getPrevNodeId() || node.getPrevNodeId() == node.getId())){
                            //new node is the new prev node for current node
                            node.setPrevNodeId(incommingNode.getId());
                            //Check if first node of network?
                            if(node.getId() == node.getNextNodeId())
                                node.setNextNodeId(incommingNode.getId());  

                            System.out.println("\r\nnode is above the next node \r\n");
                            send = true;
                        }
                        
                        //Closing the ring checks
                        if(     (node.getPrevNodeId() <= incommingNode.getId() && node.getNextNodeId() <= incommingNode.getId()) || 
                                (node.getId() < incommingNode.getId() && node.getId() == node.getPrevNodeId())){ //The incomming node is a new end node.
                            if(!send && node.getPrevNodeId() >= node.getNextNodeId()){
                                if(node.getId() > node.getNextNodeId())
                                    node.setNextNodeId(incommingNode.getId());
                                else
                                    node.setPrevNodeId(incommingNode.getId());
                                send = true;
                                System.out.println("\r\n new end node!\r\n");
                            }
                        }
                        
                        if (   (node.getPrevNodeId() >= incommingNode.getId() && node.getNextNodeId() >= incommingNode.getId()) ||
                                    (node.getId() > incommingNode.getId() && node.getId() == node.getNextNodeId())){ //The incomming node is a new start node.
                            if(!send && node.getPrevNodeId() >= node.getNextNodeId()){
                                if(node.getId() > node.getNextNodeId())
                                    node.setNextNodeId(incommingNode.getId());
                                else
                                    node.setPrevNodeId(incommingNode.getId());
                                send = true;
                                System.out.println("\r\n new start node!\r\n");
                            }
                        }

                        //The send boolean is not needed as the uncomming node will check the compatability of the ID's itself. it is just to reduce the network traffic.
                        if(send){
                        //Compose message and send out
                        UNAMNObject reply = new UNAMNObject( eMessageTypes.UnicastNodeToNode, node.getId(), node.getPrevNodeId(), node.getNextNodeId() );       
                        multicastService.sendReply(reply, incommingNode);

                        System.out.println(" \r\nThe node has been updated!");
                        System.out.println(node + "\r\n");
                        }
                        else System.out.println("Node doesn't need to be updated.");
                    }
                    
                    nodeState = eNodeState.Transfer;
                }
                case Transfer -> {
                    //TODO:
                    nodeState = eNodeState.Listening;
                }
                case Shutdown -> {
                    //TODO
                    //Gracefully, update the side nodes on its own and leave the ring topology.
                }
                case Error -> {
                    //TODO
                    //Hard, only transmit to naming server and the naming server needs to deal with it.
                    isRunning = false;
                }
                case null, default -> {
                    //Same as error?
                }
            }
        }

        //SpringApplication.run(DsProjectApplication.class, args);
    }
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
    
}
