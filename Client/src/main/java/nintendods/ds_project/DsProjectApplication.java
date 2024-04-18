package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Spring Boot application for managing a distributed system node's lifecycle excluding database auto-configuration.
 */
//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
//public class DsProjectApplication {
//
//    private static ClientNode node; // Represents the current node in the network
//
//    private static final int NODE_NAME_LENGTH = 20; // Length of the random node name
//    private static final int NODE_GLOBAL_PORT = 21; // Fixed port for node operations
//
//    private static final int DISCOVERY_RETRIES = 6; // Maximum number of retries for discovery
//    private static final int DISCOVERY_TIMEOUT = 2000; //In microseconds: Timeout for discovery
//
//    private static final int LISTENER_BUFFER_SIZE = 20; // Buffer size for the listener service
//
//    private static final String MULTICAST_ADDRESS = "224.0.0.100"; // Multicast address for network communication
//    private static final int MULTICAST_PORT = 12345; // Port for multicast communication
//
//    public static void main(String[] args) throws IOException {
//        // Initialize the node with a random name and specific network settings
//
//        // Create Node
//
//        node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
//        System.out.println("New node with name: " + node.getName() + " And hash: " + node.getId());
//
//        eNodeState nodeState = eNodeState.Discovery; // Initial state of the node
//        boolean isRunning = true; // Controls the main loop
//        ListenerService listenerService = null; // Service for handling incoming messages
//
//        int discoveryRetries = 0; // Counter for discovery attempts
//
//        while (isRunning) {
//            // Finite state machine with eNodeState states
//            switch (nodeState) {
//                case Discovery -> {
//                    // Set Discovery on
//
//                    if (discoveryRetries == DISCOVERY_RETRIES) {
//                        // Max retries reached
//                        System.out.println("Max discovery retries reached");
//                        nodeState = eNodeState.Error;
//                        break;
//                    }
//
//                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, DISCOVERY_TIMEOUT);
//                    discoveryRetries++;
//                    try {
//                        node = ds.discover(node); // Attempt to discover other nodes and establish itself in the network
//                    } catch (Exception e) {
//                        System.out.println("Retried discovery for the" + discoveryRetries + "(th) time");
//                        nodeState = eNodeState.Discovery;
//                        break;
//                    }
//                    System.out.println(node.toString());
//                    System.out.println("Successfully reply in " + discoveryRetries + " discoveries.");
//                    nodeState = eNodeState.Listening; // Move to Listening state after successful discovery
//                }
//                case Listening -> {
//                    if(listenerService == null)
//                        listenerService = new ListenerService(MULTICAST_ADDRESS, MULTICAST_PORT, LISTENER_BUFFER_SIZE);
//
//                    try {
//                        listenerService.listenAndUpdate(node); // Listen for and process incoming messages
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        nodeState = eNodeState.Error; // Move to Error state on exception
//                    }
//
//                    nodeState = eNodeState.Transfer; // Assume Transfer state is next step
//                }
//                case Transfer -> {
//                    // TODO: Transfer data or handle other operations
//                    nodeState = eNodeState.Listening; // Loop back to Listening for simplicity
//                }
//                case Shutdown -> {
//                    // TODO: Handle shutdown process, ensuring all connections are closed properly
//                    // Gracefully, update the side nodes on its own and leave the ring topology.
//                }
//                case Error -> {
//                    // TODO: Handle error state, possibly attempt to recover or shutdown gracefully
//                    // Hard, only transmit to naming server and the naming server needs to deal with
//                    // it.
//                    isRunning = false; // Stop the main loop
//                }
//                case null, default -> {
//                    // Same as error?
//                }
//            }
//        }
//
//        // SpringApplication.run(DsProjectApplication.class, args);
//    }
//
//    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // Characters used in the random string generation
//
//    /**
//     * Generates a random alphanumeric string of specified length.
//     * @param length The length of the string to generate.
//     * @return A random alphanumeric string.
//     */
//    public static String generateRandomString(int length) {
//        StringBuilder sb = new StringBuilder(length);
//        for (int i = 0; i < length; i++) {
//            int randomIndex = (int) (Math.random() * CHARACTERS.length());
//            sb.append(CHARACTERS.charAt(randomIndex));
//        }
//        return sb.toString();
//    }
//
//}

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
//import nintendods.ds_project.service.TransferService; // Assuming you have a service to handle data transfers
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DsProjectApplication {

    private static ClientNode node;
    private static eNodeState nodeState;
    private static final int NODE_NAME_LENGTH = 20; // Length of the random node name
    private static final int NODE_GLOBAL_PORT = 21; // Fixed port for node operations

    private static final int DISCOVERY_RETRIES = 6; // Maximum number of retries for discovery
    private static final int DISCOVERY_TIMEOUT = 2000; //In microseconds: Timeout for discovery

    private static final int LISTENER_BUFFER_SIZE = 20; // Buffer size for the listener service

    private static final String MULTICAST_ADDRESS = "224.0.0.100"; // Multicast address for network communication
    private static final int MULTICAST_PORT = 12345; // Port for multicast communication
    private static boolean isRunning = true;

    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext context = SpringApplication.run(DsProjectApplication.class, args);
        runNodeLifecycle(context);
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // Characters used in the random string generation

    /**
     * Generates a random alphanumeric string of specified length.
     * @param length The length of the string to generate.
     * @return A random alphanumeric string.
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    private static void runNodeLifecycle(ApplicationContext context) throws UnknownHostException {
        DiscoveryService discoveryService = context.getBean(DiscoveryService.class);
        //ListenerService listenerService = null; //context.getBean(ListenerService.class);
        //TransferService transferService = context.getBean(TransferService.class); // Assuming this service exists

        // Initialize the node with a random name and specific network settings

        // Create Node

        node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
        System.out.println("New node with name: " + node.getName() + " And hash: " + node.getId());

        nodeState = eNodeState.Discovery; // Initial state of the node
        boolean isRunning = true; // Controls the main loop
        ListenerService listenerService = null; // Service for handling incoming messages

        int discoveryRetries = 0; // Counter for discovery attempts

        while (isRunning) {
            // Finite state machine with eNodeState states
            switch (nodeState) {
                case Discovery -> {
                    // Set Discovery on

                    if (discoveryRetries == DISCOVERY_RETRIES) {
                        // Max retries reached
                        System.out.println("Max discovery retries reached");
                        nodeState = eNodeState.Error;
                        break;
                    }

                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, DISCOVERY_TIMEOUT);
                    discoveryRetries++;
                    try {
                        node = ds.discover(node); // Attempt to discover other nodes and establish itself in the network
                    } catch (Exception e) {
                        System.out.println("Retried discovery for the" + discoveryRetries + "(th) time");
                        nodeState = eNodeState.Discovery;
                        break;
                    }
                    System.out.println(node.toString());
                    System.out.println("Successfully reply in " + discoveryRetries + " discoveries.");
                    nodeState = eNodeState.Listening; // Move to Listening state after successful discovery
                }
                case Listening -> {
                    //System.out.println("Entering Listening");
                    if(listenerService == null) {
                        listenerService = new ListenerService(MULTICAST_ADDRESS, MULTICAST_PORT, LISTENER_BUFFER_SIZE);
                        listenerService.initialize_multicast();
                    }
                    try {
                        listenerService.listenAndUpdate(node); // Listen for and process incoming messages
                    } catch (Exception e) {
                        e.printStackTrace();
                        nodeState = eNodeState.Error; // Move to Error state on exception
                    }

                    nodeState = eNodeState.Transfer; // Assume Transfer state is next step
                }
                case Transfer -> {
                    // TODO: Transfer data or handle other operations
                    nodeState = eNodeState.Listening; // Loop back to Listening for simplicity
                }
                case Shutdown -> {
                    // TODO: Handle shutdown process, ensuring all connections are closed properly
                    // Gracefully, update the side nodes on its own and leave the ring topology.
                }
                case Error -> {
                    // TODO: Handle error state, possibly attempt to recover or shutdown gracefully
                    // Hard, only transmit to naming server and the naming server needs to deal with
                    // it.
                    isRunning = false; // Stop the main loop
                }
                case null, default -> {
                    // Same as error?
                }
            }
        }


    }

    /*@Bean
    public DiscoveryService discoveryService() {
        return new DiscoveryService("224.0.0.100", 12345, 2000);
    }*/

    /*@Bean
    public ListenerService listenerService() {
        return new ListenerService("224.0.0.100", 12345, 1024);
    }*/

    /*
    @Bean
    public TransferService transferService() {
        return new TransferService(); // Assuming this class exists
    }
     */
}
