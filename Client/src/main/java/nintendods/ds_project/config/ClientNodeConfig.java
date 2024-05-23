package nintendods.ds_project.config;

import jakarta.annotation.PostConstruct;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.utility.Generator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class ClientNodeConfig {

    @Value("${NODE_NAME_LENGTH}")
    private int nodeNameLength; // Length of the random node name
    @Value("${NODE_GLOBAL_PORT}")
    private int nodeGlobalPort; //Fixed port for node operations
    @Value("${DISCOVERY_RETRIES}")
    private int discoveryRetries; // Maximum number of retries for discovery
    @Value("${DISCOVERY_ADDITION_TIMEOUT}")
    private int discoveryAdditionTimeout; //In microseconds
    @Value("${LISTENER_BUFFER_SIZE}")
    private int listenerBufferSize; // Buffer size for the listener service
    @Value("${MULTICAST_ADDRESS}")
    private String multicastAddress; // Multicast address for network communication
    @Value("${MULTICAST_PORT}")
    private int multicastPort; // Port for multicast communication
    @Value("${TESTING}")
    private int testing;
    @Value("${tcp.file.receive.port}")
    private int tcpFileReceivePort;
    @Value("${tcp.file.receive.buffer}")
    private int tcpFileReceiveBuffer;

    public static int NODE_NAME_LENGTH; // Length of the random node name
    public static int NODE_GLOBAL_PORT; //Fixed port for node operations
    public static int DISCOVERY_RETRIES; // Maximum number of retries for discovery
    public static int DISCOVERY_ADDITION_TIMEOUT; //In microseconds
    public static int LISTENER_BUFFER_SIZE; // Buffer size for the listener service
    public static String MULTICAST_ADDRESS; // Multicast address for network communication
    public static int MULTICAST_PORT; // Port for multicast communication
    public static int TCP_FILE_RECEIVE_PORT;
    public static int TCP_FILE_RECEIVE_BUFFER;
    public static int TESTING;

    @PostConstruct
    private void init() {
        NODE_NAME_LENGTH = nodeNameLength;
        NODE_GLOBAL_PORT = nodeGlobalPort;
        DISCOVERY_RETRIES = discoveryRetries;
        DISCOVERY_ADDITION_TIMEOUT = discoveryAdditionTimeout;
        LISTENER_BUFFER_SIZE = listenerBufferSize;
        MULTICAST_ADDRESS = multicastAddress;
        MULTICAST_PORT = multicastPort;
        TCP_FILE_RECEIVE_PORT = tcpFileReceivePort;
        TCP_FILE_RECEIVE_BUFFER = tcpFileReceiveBuffer;
        TESTING = testing;
    }

    @Bean
    public ClientNode clientNode() throws Exception {
         return new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, Generator.randomString(NODE_NAME_LENGTH));
    }
}
