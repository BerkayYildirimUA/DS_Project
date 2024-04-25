package nintendods.ds_project;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.exeption.DuplicateNodeException;
import nintendods.ds_project.exeption.NotEnoughMessageException;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.utility.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spring Boot application for managing a distributed system node's lifecycle excluding database auto-configuration.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DsProjectApplication {

    private static ClientNode node;

    @Autowired
    public void setNode(ClientNode node) {
        DsProjectApplication.node = node;
    }

    private static eNodeState nodeState;

    private static int discoveryTimeout = 500; //In microseconds: Timeout for discovery

    private static int NODE_NAME_LENGTH; // Length of the random node name
    private static int NODE_GLOBAL_PORT; //Fixed port for node operations
    private static int DISCOVERY_RETRIES; // Maximum number of retries for discovery
    private static int DISCOVERY_ADDITION_TIMEOUT; //In microseconds
    private static int LISTENER_BUFFER_SIZE; // Buffer size for the listener service
    private static String MULTICAST_ADDRESS; // Multicast address for network communication
    private static int MULTICAST_PORT; // Port for multicast communication

    @PostConstruct
    private void init() {
        NODE_NAME_LENGTH = ClientNodeConfig.NODE_NAME_LENGTH;
        NODE_GLOBAL_PORT = ClientNodeConfig.NODE_GLOBAL_PORT;
        DISCOVERY_RETRIES = ClientNodeConfig.DISCOVERY_RETRIES;
        DISCOVERY_ADDITION_TIMEOUT = ClientNodeConfig.DISCOVERY_ADDITION_TIMEOUT;
        LISTENER_BUFFER_SIZE = ClientNodeConfig.LISTENER_BUFFER_SIZE;
        MULTICAST_ADDRESS = ClientNodeConfig.MULTICAST_ADDRESS;
        MULTICAST_PORT = ClientNodeConfig.MULTICAST_PORT;
    }

    private static UNAMObject nsObject;
    private static final CountDownLatch latch = new CountDownLatch(1);



    private static boolean isRunning = true;
    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext context = SpringApplication.run(DsProjectApplication.class, args);
        runNodeLifecycle(context);
    }

    @PreDestroy
    public void prepareForShutdown() throws InterruptedException {
        System.out.println("Preparing for shutdown...");
        nodeState = eNodeState.Shutdown;
        latch.await();
    }

    private static void runNodeLifecycle(ApplicationContext context) throws UnknownHostException {
        DiscoveryService discoveryService = context.getBean(DiscoveryService.class);
        //MulticastSendService multicastSendService = context.getBean(MulticastSendService.class);
        //ListenerService listenerService = null; //context.getBean(ListenerService.class);
        //TransferService transferService = context.getBean(TransferService.class); // Assuming this service exists
        // Initialize the node with a random name and specific network settings
        // Create Node
        System.out.println("New node with name: " + node.getName() + " And hash: " + node.getId());
        nodeState = eNodeState.Discovery; // Initial state of the node
        boolean isRunning = true; // Controls the main loop
        ListenerService listenerService = null; // Service for handling incoming messages
        int discoveryRetries = 0; // Counter for discovery attempts

        int skip = 10000;
        Boolean isDebug = Boolean.TRUE;
        while (isRunning) {

            if (skip <= 0){
                System.out.println("WE ARE TRYING TO STOP NOW");
                nodeState = eNodeState.Shutdown;
            } else if (isDebug) {
                skip -= 1;
            }

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
                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, discoveryTimeout);
                    try {
                        System.out.println("do discovery with node");
                        System.out.println(node);

                        ds.discover(node);
                        System.out.println("Discovery done");
                    }
                    catch (Exception e) {
                        discoveryRetries++;
                        if (discoveryRetries != DISCOVERY_RETRIES +1) { System.out.println("Retry discovery for the" + discoveryRetries + "(th) time"); }
                        nodeState = eNodeState.Discovery;

                        if(e instanceof DuplicateNodeException){
                            //Create new node
                            node.setName(Generator.randomString(NODE_NAME_LENGTH));
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
                    shutdown();
                    latch.countDown();
                    isRunning = false;
                    // TODO: Handle shutdown process, ensuring all connections are closed properly
                    // Gracefully, update the side nodes on its own and leave the ring topology.
                }
                case Error -> {
                    // TODO: Handle error state, possibly attempt to recover or shutdown gracefully
                    // Hard, only transmit to naming server and the naming server needs to deal with
                    // it.
                    isRunning = false; // Stop the main loop
                }
                default -> {
                    // Same as error?
                }
            }
        }
    }


    private static void shutdown(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> getNextNodeIDResponse = restTemplate.getForEntity("http://" + nsObject.getNSAddress() + ":8089/node/" + node.getNextNodeId(), String.class);
        String nextNodeIP = getNextNodeIDResponse.getBody();

        ResponseEntity<String> getPrevNodeIDResponse = restTemplate.getForEntity("http://" + nsObject.getNSAddress() + ":8089/node/" + node.getPrevNodeId(), String.class);
        String prevNodeIP = getPrevNodeIDResponse.getBody();

        // in PREV node I need to change their NEXT node to my NEXT node
        // http heeft 1 '/' in de plaats van 2 want the IP's strings starten met '/' en ik will dit niet uit filtreren.
        restTemplate.put("http:/" + prevNodeIP + ":8080/api/Management/nextNodeID/?ID=" + node.getNextNodeId(), String.class);

        // in NEXT node I need to change their PREV node to my PREV node
        restTemplate.put("http:/" + nextNodeIP + ":8080/api/Management/prevNodeID/?ID=" + node.getPrevNodeId(), String.class);

        restTemplate.delete("http://" + nsObject.getNSAddress() + ":8089/nodes/" + node.getId());
    }



}