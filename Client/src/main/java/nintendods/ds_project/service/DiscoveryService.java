package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.*;
import nintendods.ds_project.utility.JsonConverter;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryService {
    private List<String> receivedMessages; // List to hold received messages
    private String multicastAddress = "224.0.0.100"; // Multicast IP address for discovery
    private int multicastPort = 12345; // Port number for multicast communication
    private int waitTimeDiscovery = 20000; // Time to wait for discovery responses in milliseconds
    private UDPServer listener; // UDP server for listening to responses
    private ServerSocket socket; // Server socket for communication

    /**
     * Default constructor setting up basic properties
     * Create a Discovery service object.
     * Will send out a discovery message with format {@link MNObject} and waits for
     * a response of the Naming server of type {@link UNAMObject}.
     */
    public DiscoveryService() {
        // Setup the socket and get the port
        this.receivedMessages = new ArrayList<>();
    }

    /**
     * Constructor for creating a DiscoveryService instance.
     *
     * @param multicastAddress The multicast address to be used for discovery.
     * @param multicastPort    The port number on which multicast communication will
     *                         occur.
     * @param waitTime         The time duration the discovery service will wait for
     *                         responses in miliseconds.
     */
    public DiscoveryService(String multicastAddress, int multicastPort, int waitTime) {
        // Setup the socket and get the port
        // Create server socket where we'll listen on when the multicast is sended out.
        try {
            this.socket = new ServerSocket(0); // Initialize the server socket on any available port
            this.listener = new UDPServer(InetAddress.getLocalHost(), socket.getLocalPort(), 256); // Setup UDP server
        } catch (Exception ex) { // TODO: Handle exceptions such as socket errors, this should go to error state.
        }
        this.receivedMessages = new ArrayList<>();
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.waitTimeDiscovery = waitTime;
    }

    public ClientNode discover(ABaseNode node) throws Exception {
        // Set up the UDPServer for listening to UDP responses
        Thread udpListenerThread = new Thread(() -> {
            try {
                udpListener(this.waitTimeDiscovery, this.listener);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Create multicast object
        MulticastSendService ms = new MulticastSendService(multicastAddress, multicastPort);
        long udp_id = System.currentTimeMillis(); // unique messageID based on timestamp

        // start listener thread
        udpListenerThread.start();

        // Send out multicast discovery messages
        // Those messages are sent out 2 times because of UDP (multicast message).
        // If there is no answer, the service will go in timeout for 2 seconds and then repeat the process.
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, InetAddress.getLocalHost().getHostAddress(),
                this.socket.getLocalPort(), node.getName()));
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, InetAddress.getLocalHost().getHostAddress(),
                socket.getLocalPort(), node.getName()));

        // Wait for UDP packet to be filled in.
        while (udpListenerThread.isAlive())
            ;

        JsonConverter jsonConverter = new JsonConverter();

        // Do some processing of the data

        if (receivedMessages.size() == 0)
            throw new Exception("No messages received within the timeframe");

        // reformat the list to unique messages to avoid duplicates
        List<AMessage> filteredMessages = new ArrayList<>();
        for (String message : receivedMessages) {
            AMessage m = null;

            // check for type conversion
            if (message.contains(eMessageTypes.UnicastNamingServerToNode.toString()))
                m = (UNAMObject) jsonConverter.toObject(message, UNAMObject.class);
            if (message.contains(eMessageTypes.UnicastNodeToNode.toString()))
                m = (UNAMNObject) jsonConverter.toObject(message, UNAMNObject.class);

            if (m == null)
                throw new Exception("no cast found!");

            // Filter out double messages
            long messageId = m.getMessageId();
            if (filteredMessages.stream().noneMatch(mes -> mes.getMessageId() == messageId)) {
                filteredMessages.add(m);
            }
        }

        ClientNode newNode = (ClientNode) node; // Casting
        int prevId = -1;
        int nextId = -1;

        // Check the amount of nodes present in the network
        int numberOfNodes = ((UNAMObject) filteredMessages.stream()
                .filter(m -> m.getMessageType() == eMessageTypes.UnicastNamingServerToNode).toList().getFirst())
                .getAmountOfNodes();
        if (numberOfNodes > 1) {
            // More than 1 so use neighbour nodes its data to form the prev and next node.

            // fetch the other messages as UNAMNObjects if possible
            if (filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList()
                    .size() == 0)
                throw new Exception("Not enough nodes have send out their multicast response!");
            List<UNAMNObject> nodeMessages = new ArrayList<>();
            for (AMessage m : filteredMessages.stream()
                    .filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList())
                nodeMessages.add((UNAMNObject) m);
            // fetch other data from other nodes.
            if (nodeMessages.size() > 0) // check if 2 nodes send their info. Already checked above.
            {
                try {
                    prevId = nodeMessages.stream().filter(m -> m.getNextNodeId() == ((ClientNode) node).getId())
                            .toList().getFirst().getNodeHashId();
                } catch (Exception ex) {
                }
                try {
                    nextId = nodeMessages.stream().filter(m -> m.getPrevNodeId() == ((ClientNode) node).getId())
                            .toList().getFirst().getNodeHashId();
                } catch (Exception ex) {
                }
            }
        }

        // Assign value to node
        newNode.setNextNodeId(nextId);
        newNode.setPrevNodeId(prevId);

        System.out.println("\r\nDiscoveryService - New node composed");
        // System.out.println("\t"+newNode);
        return newNode;
    }

    private void udpListener(int timeOutTime, UDPServer listener) throws Exception {
        boolean timeout = false;
        long startTimestamp = System.currentTimeMillis();

        while (!timeout) {
            try {
                receivedMessages.add(listener.listen(2000)); // Listen for messages, timeout every 2000 milliseconds
                // receivedMessages.forEach(System.out::println);
            } catch (SocketTimeoutException ignored) { // TODO: Timeout is normal and ignored here
            }
            // if the messages are received within a specific time (totalTime seconds)
            if (startTimestamp + timeOutTime < System.currentTimeMillis())
                timeout = true;
        }

        listener.close(); // Close listener after the wait time has expired
    }
}