package nintendods.ds_project;

import nintendods.ds_project.exeption.DuplicateNodeException;
import nintendods.ds_project.exeption.NotEnoughMessageException;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
import nintendods.ds_project.service.NSAPIService;
import nintendods.ds_project.utility.JsonConverter;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DsProjectApplication {

    private static ClientNode node;

    private static NSAPIService nsapiService;

    private static final int NODE_NAME_LENGTH = 20;
    private static final int NODE_GLOBAL_PORT = 21;

    private static final int DISCOVERY_RETRIES = 10;
    private static int discoveryTimeout = 500; //In microseconds
    private static final int DISCOVERY_ADDITION_TIMEOUT = 1000; //In microseconds

    private static final int LISTENER_BUFFER_SIZE = 20;

    private static final String MULTICAST_ADDRESS = "224.0.0.100";
    private static final int MULTICAST_PORT = 12345;

    public static void main(String[] args) throws IOException {
        // Create Node

        node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
        
        eNodeState nodeState = eNodeState.Discovery;
        boolean isRunning = true;
        ListenerService listenerService = null;
        JsonConverter jsonConverter = new JsonConverter();
        UNAMObject nsObject;

        int discoveryRetries = 0;

        while (isRunning) {
            // Finite state machine with eNodeState states
            switch (nodeState) {
                case Discovery -> {
                    // Set Discovery on

                    if (discoveryRetries == DISCOVERY_RETRIES +1) {
                        // Max retries reached
                        System.out.println("Max discovery retries reached");
                        nodeState = eNodeState.Error;
                        break;
                    }

                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, discoveryTimeout);
                    try {
                        System.out.println("do discovery with node");
                        System.out.println(node);

                        node = ds.discover(node);
                        System.out.println("Discovery done");
                    } 
                    catch (Exception e) {
                        discoveryRetries++;
                        if (discoveryRetries != DISCOVERY_RETRIES +1) System.out.println("Retry discovery for the" + discoveryRetries + "(th) time");
                        nodeState = eNodeState.Discovery;

                        if(e instanceof DuplicateNodeException){
                            //Create new node
                            node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
                            System.out.println(node);
                            System.out.println("nodeName updated " + node.getName());
                        }
                        if(e instanceof NotEnoughMessageException){
                            //Create other timeout
                            discoveryTimeout +=  DISCOVERY_ADDITION_TIMEOUT;
                            System.out.println("discoveryTimeout updated " + discoveryTimeout);
                        }

                        break;
                    }
                    
                    // //Discovery has succeeded so continue
                    // //get NSObject from discovery service
                    nsObject = ds.getNSObject(); //For later use

                    System.out.println(node.toString());
                    System.out.println("Successfully reply in " + discoveryRetries + " discoveries.");
                    nodeState = eNodeState.Listening;
                }
                case Listening -> {
                    if (listenerService == null)
                        listenerService = new ListenerService(MULTICAST_ADDRESS, MULTICAST_PORT, LISTENER_BUFFER_SIZE);
                    try {
                        listenerService.listenAndUpdate(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                        nodeState = eNodeState.Error;
                    }

                    nodeState = eNodeState.Transfer;
                }
                case Transfer -> {
                    // TODO:
                    nodeState = eNodeState.Listening;
                }
                case Shutdown -> {
                    // TODO
                    // Gracefully, update the side nodes on its own and leave the ring topology.

                    /**
                    * When the node gets in the Shutdown state inside the discovery service, we'll access the 
                    * NamingServer API to handle everything from here.
                    * call: {NSAddress}:{NSPort}/nodes/{id}/shutdown
                    */
                }
                case Error -> {
                    // TODO
                    // Hard, only transmit to naming server and the naming server needs to deal with
                    // it.

                    /**
                    * When the node gets in the Error state, we'll access the 
                    * NamingServer API to handle everything from here.
                    * call: {NSAddress}:{NSPort}/nodes/{id}/error
                    */

                    isRunning = false;
                }
                case null, default -> {
                    // Same as error?
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

    public static int generateRandomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

}
