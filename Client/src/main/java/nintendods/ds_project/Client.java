package nintendods.ds_project;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.controller.ClientManagementAPI;
import nintendods.ds_project.exeption.DuplicateNodeException;
import nintendods.ds_project.exeption.NotEnoughMessageException;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
import nintendods.ds_project.utility.Generator;
import org.hibernate.annotations.Synchronize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Spring Boot application for managing a distributed system node's lifecycle excluding database auto-configuration.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Client {

    public ClientNode getNode() {
        return node;
    }

    @Autowired
    public void addNode(ClientNode node){
        this.node = node;
    }

    @Autowired
    ApplicationContext context;
    private eNodeState nodeState;
    private int discoveryTimeout = 500; //In microseconds: Timeout for discovery

    private int NODE_NAME_LENGTH;      // Length of the random node name
    private int NODE_GLOBAL_PORT;      //Fixed port for node operations
    private int DISCOVERY_RETRIES;      // Maximum number of retries for discovery
    private int DISCOVERY_ADDITION_TIMEOUT;     //In microseconds
    private int LISTENER_BUFFER_SIZE;     // Buffer size for the listener service
    private String MULTICAST_ADDRESS;   // Multicast address for network communication
    private int MULTICAST_PORT;      // Port for multicast communication
    private UNAMObject nsObject;
    private boolean isRunning = true;

    @Value("${server.port}")
    private int apiPort;



    //vars needed for testing
    private int testing;
    public int t_nextNodePort;
    public int t_prevNodePort;


    public static void main(String[] args) throws UnknownHostException {
        SpringApplication.run(DsProjectApplication.class, args);
    }


    @PostConstruct
    private void init() throws UnknownHostException {
        NODE_NAME_LENGTH = ClientNodeConfig.NODE_NAME_LENGTH;
        NODE_GLOBAL_PORT = ClientNodeConfig.NODE_GLOBAL_PORT;
        DISCOVERY_RETRIES = ClientNodeConfig.DISCOVERY_RETRIES;
        DISCOVERY_ADDITION_TIMEOUT = ClientNodeConfig.DISCOVERY_ADDITION_TIMEOUT;
        LISTENER_BUFFER_SIZE = ClientNodeConfig.LISTENER_BUFFER_SIZE;
        MULTICAST_ADDRESS = ClientNodeConfig.MULTICAST_ADDRESS;
        MULTICAST_PORT = ClientNodeConfig.MULTICAST_PORT;
        testing = ClientNodeConfig.TESTING;

        if (testing == 1){
            t_prevNodePort = 0;
            t_nextNodePort = 0;
        }

      //  runNodeLifecycle(context);
    }

    @PreDestroy
    public void prepareForShutdown() throws InterruptedException {
        if (nodeState != eNodeState.Discovery) {
            System.out.println("Preparing for shutdown...");
            shutdown();
            System.out.println("Nodes prepared.");
            isRunning = false;
            System.out.println("PreDestroy done");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        new Thread(this::runNodeLifecycle).start();
    }

    private void runNodeLifecycle() {
        DiscoveryService discoveryService = context.getBean(DiscoveryService.class);
        //MulticastSendService multicastSendService = context.getBean(MulticastSendService.class);
        //ListenerService listenerService = null; //context.getBean(ListenerService.class);
        //TransferService transferService = context.getBean(TransferService.class); // Assuming this service exists
        // Initialize the node with a random name and specific network settings
        // Create Node
        logger.info("New node with name: " + node.getName() + " And hash: " + node.getId());
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
                        logger.warn("Max discovery retries reached");
                        nodeState = eNodeState.Error;
                        break;
                    }
                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, discoveryTimeout);
                    try {
                        logger.info("do discovery with node");
                        logger.info(node.toString());

                        ds.discover(node);
                        logger.info("Discovery done");

                    } catch (Exception e) {
                        discoveryRetries++;
                        if (discoveryRetries != DISCOVERY_RETRIES + 1) {
                            logger.warn("Retry discovery for the" + discoveryRetries + "(th) time");
                        }
                        nodeState = eNodeState.Discovery;

                        if (e instanceof DuplicateNodeException) {
                            //Create new node
                            node.setName(Generator.randomString(NODE_NAME_LENGTH));
                            logger.info(node.toString());
                            logger.info("nodeName updated " + node.getName());
                        }
                        if (e instanceof NotEnoughMessageException) {
                            //Create other timeout
                            discoveryTimeout += DISCOVERY_ADDITION_TIMEOUT;
                            logger.info("discoveryTimeout updated " + discoveryTimeout);
                        }
                        break;
                    }

                    // //Discovery has succeeded so continue
                    // //get NSObject from discovery service
                    nsObject = ds.getNSObject(); //For later use
                    logger.info(node.toString());
                    logger.info("Successfully reply in " + discoveryRetries + " discoveries.");
                    nodeState = eNodeState.Listening; // Move to Listening state after successful discovery
                }
                case Listening -> {
                    //System.out.println("Entering Listening");
                    if (listenerService == null) {
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
            /*        System.out.println("Prepare nodes for shutdown");
                    System.out.println("Nodes prepared. Latch Down");
                    latch.countDown();
                    isRunning = false;*/
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

        System.out.println("Main Done");
    }


    private void shutdown() {
        RestTemplate restTemplate = new RestTemplate();

        if (node.getId() != node.getPrevNodeId()) {
            int nextNodePort;
            int prevNodePort;

            String prevNodeIP;
            String nextNodeIP;


            if (testing == 1) {
                nextNodePort = t_nextNodePort;
                prevNodePort = t_prevNodePort;

                prevNodeIP = "/127.0.0.1";
                nextNodeIP = "/127.0.0.1";
            } else {
                nextNodePort = apiPort;
                prevNodePort = apiPort;

                String urlGetNextNodeID = "http://" + nsObject.getNSAddress() + ":8089/node/" + node.getNextNodeId();
                logger.info("GET from: " + urlGetNextNodeID);
                ResponseEntity<String> getNextNodeIDResponse = restTemplate.getForEntity(urlGetNextNodeID, String.class);
                nextNodeIP = getNextNodeIDResponse.getBody();

                String urlGetPrevNodeID = "http://" + nsObject.getNSAddress() + ":8089/node/" + node.getPrevNodeId();
                logger.info("GET from: " + urlGetPrevNodeID);
                ResponseEntity<String> getPrevNodeIDResponse = restTemplate.getForEntity(urlGetPrevNodeID, String.class);
                prevNodeIP = getPrevNodeIDResponse.getBody();

            }

            // in PREV node I need to change their NEXT node to my NEXT node
            // http heeft 1 '/' in de plaats van 2 want the IP's strings starten met '/' en ik will dit niet uit filtreren.
            String UrlForPrevNode = "http:/" + prevNodeIP + ":" + prevNodePort + "/api/Management/nextNodeID/?ID=" + node.getNextNodeId();
            logger.info("PUT to: " + UrlForPrevNode);
            restTemplate.put(UrlForPrevNode, String.class);

            // in NEXT node I need to change their PREV node to my PREV node
            String urlForNextNode = "http:/" + nextNodeIP + ":" + nextNodePort + "/api/Management/prevNodeID/?ID=" + node.getPrevNodeId();
            logger.info("PUT to: " + urlForNextNode);
            restTemplate.put(urlForNextNode, String.class);
        }

        String urlDeleteNode = "http://" + nsObject.getNSAddress() + ":8089/nodes/" + node.getId();
        logger.info("DELETE from: " + urlDeleteNode);
        restTemplate.delete(urlDeleteNode);
    }


}