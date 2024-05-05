package nintendods.ds_project.service;

import nintendods.ds_project.exeption.DuplicateNodeException;
import nintendods.ds_project.exeption.NotEnoughMessageException;
import nintendods.ds_project.model.ANetworkNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.*;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Component("Dis1")
public class DiscoveryService {
    private List<String> receivedMessages;
    private String multicastAddress = "224.0.0.100";
    private int multicastPort = 12345;
    private int waitTimeDiscovery = 20000;
    private UDPServer listener;
    private ServerSocket socket;
    private UNAMObject nsObject;

    /**
     * Create a Discovery service object.
     * Will send out a discovery message with format {@link MNObject} and waits for
     * a response of the Naming server of type {@link UNAMObject} and from surrounding nodes of type {@link UNAMNObject}.
     */
    public DiscoveryService() { this.receivedMessages = new ArrayList<>(); }

    /**
     * Create a Discovery service object.
     * Will send out a discovery message with format {@link MNObject} and waits for
     * a response of the Naming server of type {@link UNAMObject} and from surrounding nodes of type {@link UNAMNObject}.
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
            this.socket = new ServerSocket(0);
            this.listener = new UDPServer(InetAddress.getLocalHost(), socket.getLocalPort(), 256);
        } catch (Exception ex) { }
        this.receivedMessages = new ArrayList<>();
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.waitTimeDiscovery = waitTime;
    }
    public void discover(ClientNode node) throws Exception {
        // Set up the UDPServer
        Thread udpListenerThread = new Thread(() -> {
            try { udpListener(this.waitTimeDiscovery, this.listener); } 
            catch (Exception e) { throw new RuntimeException(e); }
        });
    //---------------------------------------BEGIN DISCOVERY---------------------------------------------//
        // Create multicast object
        MulticastSendService ms = new MulticastSendService(multicastAddress, multicastPort);
        long udp_id = System.currentTimeMillis(); // unique messageID

        // start listener
        udpListenerThread.start();

        // Send out messages
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, InetAddress.getLocalHost().getHostAddress(), socket.getLocalPort(), node.getName()));
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, InetAddress.getLocalHost().getHostAddress(), socket.getLocalPort(), node.getName()));

        // Wait for UDP packet to be filled in.
        while (udpListenerThread.isAlive());

        JsonConverter jsonConverter = new JsonConverter();

        // Do some processing of the data

        if (receivedMessages.size() == 0) { throw new NotEnoughMessageException();}

        // reformat the list to unique messages
        List<AMessage> filteredMessages = new ArrayList<>();
        for (String message : receivedMessages) {
            AMessage m = null;

            // check for type conversion
            if (message.contains(eMessageTypes.UnicastNamingServerToNode.toString())){
                m = (UNAMObject) jsonConverter.toObject(message, UNAMObject.class);
            }
            if (message.contains(eMessageTypes.UnicastNodeToNode.toString())){
                m = (UNAMNObject) jsonConverter.toObject(message, UNAMNObject.class);
            }

            if (m == null) { throw new Exception("no cast found!"); }

            // Filter out double messages
            long messageId = m.getMessageId();
            if (filteredMessages.stream().noneMatch(mes -> mes.getMessageId() == messageId)) {
                filteredMessages.add(m);
            }
        }
    //---------------------------------------END DISCOVERY---------------------------------------------//
    //---------------------------------------BEGIN BOOTSTRAP---------------------------------------------//
        int prevId = -1;
        int nextId = -1;

        // Accuire the namingserver unicast message
        nsObject = ((UNAMObject) filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNamingServerToNode).toList().getFirst());

        //Check the amount of nodes present in the network
        if (nsObject.getAmountOfNodes() >= 1) {
            // More then 1 so use neighbors nodes its data to form the prev and next node.

            // fetch the other messages as UnicastNodeToNode if possible.
            if (filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList().isEmpty()){
                throw new NotEnoughMessageException();
            }

            List<UNAMNObject> nodeMessages = new ArrayList<>();

            for (AMessage m : filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList()){
                nodeMessages.add((UNAMNObject) m);
            }

            //Check if enough messages has arrived at the node
            if(nsObject.getAmountOfNodes() == 1 && nodeMessages.size() < 1) { throw new NotEnoughMessageException(); }
            else if(nsObject.getAmountOfNodes() > 1 && nodeMessages.size() < 2) { throw new NotEnoughMessageException(); }

            // fetch other data from other nodes.
            if (!nodeMessages.isEmpty()) { // check if 2 nodes send their info. Already checked above.
                
                //Check if a received message has the same id as the node itself
                // All the nodes will send back and if a node has the same ID as the received ID, it will send also a message back. 
                // Now the discovery node can check if his ID is a duplicate in the network.
                if(nodeMessages.stream().anyMatch(m -> m.getNodeHashId() == node.getId())) {
                    System.out.println("Node is already in network!");
                    throw new DuplicateNodeException();
                }

                //Check the incomming messages from the nodes that have been changed
                try { prevId = nodeMessages.stream().filter(m -> m.getNextNodeId() == ( node).getId()).toList().getFirst().getNodeHashId(); }
                catch (Exception ex) { }

                try { nextId = nodeMessages.stream().filter(m -> m.getPrevNodeId() == ( node).getId()).toList().getFirst().getNodeHashId(); }
                catch (Exception ex) { }
            }
        }
        else if (!filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList().isEmpty()){
            //Special case when only 1 node is present. If the same ID, it will be amountOfNodes = 0;
            List<UNAMNObject> nodeMessages = new ArrayList<>();

            for (AMessage m : filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList()){
                nodeMessages.add((UNAMNObject) m);
            }

            //Check if a received message has the same id as the node itself
            // All the nodes will send back and if a node has the same ID as the received ID, it will send also a message back. 
            // Now the discovery node can check if his ID is a duplicate in the network.
            if (nodeMessages.stream().anyMatch(m -> m.getNodeHashId() == node.getId()))
            {
                System.out.println("Node is already in network!");
                throw new DuplicateNodeException();
            }
        }

        // Assign value to node
        node.setNextNodeId(nextId);
        node.setPrevNodeId(prevId);

        System.out.println("\r\nDiscoveryService - New node composed");
        return newNode;
    //---------------------------------------END BOOTSTRAP---------------------------------------------//
    }

    private void udpListener(int timeOutTime, UDPServer listener) throws Exception {
        boolean timeout = false;
        long startTimestamp = System.currentTimeMillis();

        while (!timeout) {
            try { receivedMessages.add(listener.listen()); } 
            catch (SocketTimeoutException ignored) { }
            if (startTimestamp + timeOutTime < System.currentTimeMillis()) { timeout = true; }
        }

        listener.close();
    }

    //later use
    public UNAMObject getNSObject(){
         return nsObject;
    }
}