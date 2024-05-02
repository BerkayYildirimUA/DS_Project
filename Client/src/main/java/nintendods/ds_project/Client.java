package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
import nintendods.ds_project.service.NSAPIService;
import nintendods.ds_project.service.UnicastListenService;
import nintendods.ds_project.utility.JsonConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Client {

    private static ClientNode node;

    private static NSAPIService nsapiService;
    private static UnicastListenService unicastService;

    private static final int NODE_NAME_LENGTH = 20;
    private static final int NODE_GLOBAL_PORT = 23;

    private static final int DISCOVERY_RETRIES = 6;
    private static final int DISCOVERY_TIMEOUT = 8000; //In microseconds

    private static final int LISTENER_BUFFER_SIZE = 20;

    private static final String MULTICAST_ADDRESS = "224.0.0.100";
    private static final int MULTICAST_PORT = 12345;

    public static void main(String[] args) throws IOException {
        // Create Node

        node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
        System.out.println(node);

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
                    if (discoveryRetries == DISCOVERY_RETRIES) {
                        // Max retries reached
                        System.out.println("Max discovery retries reached");
                        nodeState = eNodeState.Error;
                        break;
                    }

                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, DISCOVERY_TIMEOUT);
                    discoveryRetries++;
                    try {
                        node = ds.discover(node);
                    } catch (Exception e) {
                        System.out.println("Retried discovery for the " + discoveryRetries + "(th) time");
                        nodeState = eNodeState.Discovery;
                        //Create new node
                        node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
                        System.out.println(node);
                        break;
                    }
                    
                    // //Discovery has succeeded so continue
                    // //get NSObject from discovery service
                    nsObject = ds.getNSObject();

                    // //Define the api object
                    nsapiService = new NSAPIService(nsObject.getNSAddress(), nsObject.getNSPort());

                    // //Add node to Naming Server
                    // nsapiService.executePost("/nodes", jsonConverter.toJson(nsObject));

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

                    if (node.getId() < node.getPrevNodeId())    {
                        System.out.println("Client sleep");
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        nodeState = eNodeState.Error;
                    } else nodeState = eNodeState.Transfer;
                }
                case Transfer -> {
                    // TODO:
                    nodeState = eNodeState.Listening;
                }
                case Shutdown -> {
                    System.out.println("Start: SHUTDOWN\t" + Timestamp.from(Instant.now()));
                    // TODO
                    // Gracefully, update the side nodes on its own and leave the ring topology.

                    /**
                    * When the node gets in the Shutdown state inside the discovery service, we'll access the 
                    * NamingServer API to handle everything from here.
                    * call: {NSAddress}:{NSPort}/nodes/{id}/shutdown
                    */
                }
                case Error -> {
                    System.out.println("Start: ERROR\t" + Timestamp.from(Instant.now()));
                    // TODO
                    // Hard, only transmit to naming server and the naming server needs to deal with it.

                    System.out.println("Client: Send error");
                    nsapiService.executeErrorDelete("/nodes/" + node.getId() + "/error");
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
        // SpringApplication.run(DsProjectApplication.class, args);
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
