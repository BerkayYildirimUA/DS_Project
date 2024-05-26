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
    // @Value("${tcp.file.receive.port}")
    // private int tcpFileReceivePort;
    // @Value("${tcp.file.receive.buffer}")
    // private int tcpFileReceiveBuffer;
    @Value("${tcp.file.receive.port}")
    private int tcpFileReceivePort;
    @Value("${server.port}")
    private int apiPort;

    private static int NODE_NAME_LENGTH; // Length of the random node name
    private static int NODE_GLOBAL_PORT; //Fixed port for node operations
    private static int DISCOVERY_RETRIES; // Maximum number of retries for discovery
    private static int DISCOVERY_ADDITION_TIMEOUT; //In microseconds
    private static int LISTENER_BUFFER_SIZE; // Buffer size for the listener service
    private static String MULTICAST_ADDRESS; // Multicast address for network communication
    private static int MULTICAST_PORT; // Port for multicast communication
    private static int TCP_FILE_RECEIVE_PORT;
    private static int TESTING;
    private static int API_PORT;

    public static int getNodeNameLength() {
        return NODE_NAME_LENGTH;
    }

    public static int getNodeGlobalPort() {
        return NODE_GLOBAL_PORT;
    }

    public static int getDiscoveryRetries() {
        return DISCOVERY_RETRIES;
    }

    public static int getDiscoveryAdditionTimeout() {
        return DISCOVERY_ADDITION_TIMEOUT;
    }

    public static int getListenerBufferSize() {
        return LISTENER_BUFFER_SIZE;
    }

    public static String getMulticastAddress() {
        return MULTICAST_ADDRESS;
    }

    public static int getMulticastPort() {
        return MULTICAST_PORT;
    }

    public static int getTcpFileReceivePort() {
        return TCP_FILE_RECEIVE_PORT;
    }

    public static int getTESTING() {
        return TESTING;
    }

    public static int getApiPort() {
        return API_PORT;
    }

    @PostConstruct
    private void init() {
        NODE_NAME_LENGTH = nodeNameLength;
        NODE_GLOBAL_PORT = nodeGlobalPort;
        DISCOVERY_RETRIES = discoveryRetries;
        DISCOVERY_ADDITION_TIMEOUT = discoveryAdditionTimeout;
        LISTENER_BUFFER_SIZE = listenerBufferSize;
        MULTICAST_ADDRESS = multicastAddress;
        MULTICAST_PORT = multicastPort;
        // TCP_FILE_RECEIVE_PORT = tcpFileReceivePort;
        // TCP_FILE_RECEIVE_BUFFER = tcpFileReceiveBuffer;
        TESTING = testing;
        API_PORT = apiPort;
    }

    @Bean
    public ClientNode clientNode() throws Exception {
         return new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, Generator.randomString(NODE_NAME_LENGTH));
    }
}
