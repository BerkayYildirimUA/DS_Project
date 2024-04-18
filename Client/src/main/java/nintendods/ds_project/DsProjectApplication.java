package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.service.DiscoveryService;
import nintendods.ds_project.service.ListenerService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.io.IOException;
import java.net.InetAddress;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DsProjectApplication {

    private static ClientNode node;

    private static final int NODE_NAME_LENGTH = 20;
    private static final int NODE_GLOBAL_PORT = 21;

    private static final int DISCOVERY_RETRIES = 6;
    private static final int DISCOVERY_TIMEOUT = 2000; //In microseconds

    private static final int LISTENER_BUFFER_SIZE = 20;

    private static final String MULTICAST_ADDRESS = "224.0.0.100";
    private static final int MULTICAST_PORT = 12345;

    public static void main(String[] args) throws IOException {
        // Create Node

        node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
        System.out.println("New node with name: " + node.getName() + " And hash: " + node.getId());

        eNodeState nodeState = eNodeState.Discovery;
        boolean isRunning = true;
        ListenerService listenerService = null;

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
                        System.out.println("Retried discovery for the" + discoveryRetries + "(th) time");
                        nodeState = eNodeState.Discovery;
                        break;
                    }
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
                }
                case Error -> {
                    // TODO
                    // Hard, only transmit to naming server and the naming server needs to deal with
                    // it.
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

}
