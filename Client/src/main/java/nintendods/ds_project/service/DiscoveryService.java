package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.*;
import nintendods.ds_project.utility.JsonConverter;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryService {
    private List<String> receivedMessages;
    private String multicastAddress = "224.0.0.100";
    private int multicastPort = 12345;
    private int waitTimeDiscovery = 20000;
    private UDPServer listener;
    private ServerSocket socket;

    /**
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
     *                         responses.
     */
    public DiscoveryService(String multicastAddress, int multicastPort, int waitTime) {
        // Setup the socket and get the port
        // Create server socket where we'll listen on when the multicast is sended out.
        try {
            this.socket = new ServerSocket(0);
            this.listener = new UDPServer(InetAddress.getLocalHost(), socket.getLocalPort(), 256);
        } catch (Exception ex) {
        }
        this.receivedMessages = new ArrayList<>();
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.waitTimeDiscovery = waitTime;
    }

    public ClientNode discover(ABaseNode node) throws Exception {
        // Set up the UDPServer
        Thread udpListenerThread = new Thread(() -> {
            try {
                udpListener(this.waitTimeDiscovery, this.listener);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Create multicast object
        MulticastSendService ms = new MulticastSendService(multicastAddress, multicastPort);
        long udp_id = System.currentTimeMillis(); // unique messageID

        // start listener
        udpListenerThread.start();

        // Send out messages
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

        // reformat the list to unique messages
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
                // Maybe more checks before adding node id's?
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
                receivedMessages.add(listener.listen(2000));
                // receivedMessages.forEach(System.out::println);
            } catch (SocketTimeoutException ignored) {
            }
            // if the messages are received within a specific time (totalTime seconds)
            if (startTimestamp + timeOutTime < System.currentTimeMillis())
                timeout = true;
        }

        listener.close();
    }
}