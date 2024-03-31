package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.*;
import nintendods.ds_project.utility.JsonConverter;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscoveryService {
    private List<String> receivedMessages;
    private String multicastAddress = "224.0.0.100";
    private int multicastPort = 12345;

    /**
     * Create a Discovery service object.
     * Will send out a discovery message with format {@link MNObject} and waits for a response of the Naming server of type {@link UNAMObject}.
     */
    public DiscoveryService() {
        //Setup the socket and get the port
        this.receivedMessages = new ArrayList<>();
    }

    public DiscoveryService(String multicastAddress, int multicastPort) {
        //Setup the socket and get the port
        this.receivedMessages = new ArrayList<>();
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    public ClientNode discover(ABaseNode node) throws Exception {
        //Create server socket where we'll listen on when the multicast is sended out.
        ServerSocket socket = new ServerSocket(0);

        //Set up the UDPServer
        Thread udpListenerThread = new Thread(() -> udpListener(socket));


        //Send out the multicast
        MulticastService ms = new MulticastService(multicastAddress, multicastPort);
        long udp_id = System.currentTimeMillis(); // unique messageID

        //start listener
        udpListenerThread.start();

        //Send out messages
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, InetAddress.getLocalHost().getHostAddress(), socket.getLocalPort(), node.getName()));
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, InetAddress.getLocalHost().getHostAddress(), socket.getLocalPort(), node.getName()));

        //Wait for UDP packet to be filled in.
        while (udpListenerThread.isAlive()) ;

        JsonConverter jsonConverter = new JsonConverter();

        //Do some processing of the data

        //if no messages received, try a deletion of the node at the NamingServer?
        if (receivedMessages.size() == 0)
            throw new Exception("No messages received within the timeframe");

        // reformat the list to unique messages
        List<AMessage> filteredMessages = new ArrayList<>();
        for (String message : receivedMessages) {
            AMessage m = null;

            //check for type
            if (message.contains(eMessageTypes.UnicastNamingServerToNode.toString()))
                m = (UNAMObject) jsonConverter.toObject(message, UNAMObject.class);
            if (message.contains(eMessageTypes.UnicastNodeToNode.toString()))
                m = (UNAMNObject) jsonConverter.toObject(message, UNAMNObject.class);

            if (m == null) throw new Exception("no cast found!");

            //Vraag message id op en vergelijk met de opgeslagen messages.
            long messageId = m.getMessageId();
            if (filteredMessages.stream().noneMatch(mes -> mes.getMessageId() == messageId)) {
                filteredMessages.add(m);
            }
        }

        ClientNode newNode = (ClientNode) node; //Casting

        //Check the amount of nodes present in the network
        UNAMObject temp = null;
        if (filteredMessages.stream().anyMatch(m -> m.getMessageType() == eMessageTypes.UnicastNamingServerToNode)) {
            temp = (UNAMObject) filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNamingServerToNode).toList().getFirst();
        }

        assert temp != null;
        if (temp.getAmountOfNodes() > 1) {
            //More than 1 so use neighbour nodes its data to form the prev and next node.

            //fetch messages as UNAMNObjects
            List<UNAMNObject> nodeMessages = Collections.singletonList((UNAMNObject) filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList());
            if (nodeMessages.size() <= 1)
                throw new Exception("Not enough nodes have send out their multicast response!");

            //fetch other data from other nodes.
            if (filteredMessages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).count() > 1) //check if 2 nodes send their info
            {
                newNode.setPrevNodeId(nodeMessages.stream().filter(m -> m.getPrevNodeId() == -1).toList().getFirst().getNodeHashId());//Get the node with lowest ID and add to prevNodeId
                newNode.setNextNodeId(nodeMessages.stream().filter(m -> m.getNextNodeId() == -1).toList().getFirst().getNodeHashId());//Get the node with highest ID and add to prevNodeId
                //Maybe more checks before adding node id's?
            }
        } else if (temp.getAmountOfNodes() < 1) {
            // no nodes in the network so set prev and next node as its own hash ID.
            newNode.setNextNodeId(newNode.getId());
            newNode.setPrevNodeId(newNode.getId());
        }
        return newNode;
    }

    private void udpListener(ServerSocket socket) throws Exception {
        UDPServer listener = new UDPServer(InetAddress.getLocalHost(), socket.getLocalPort(), 256);

        boolean timeout = false;

        long startTimestamp = System.currentTimeMillis();
        long totalTime = 10000; //10 seconds

        while (!timeout) {
            try {
                receivedMessages.add(listener.listen(500));
            } catch (SocketTimeoutException ignored) {
            }
            //if the messages are received within a specific time (totalTime seconds)
            if (startTimestamp + totalTime < System.currentTimeMillis()) {
                System.out.println("Final timeout");
                timeout = true;
            }
        }

        listener.close();
    }
}