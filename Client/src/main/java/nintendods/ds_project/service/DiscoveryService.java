package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.*;
import nintendods.ds_project.utility.JsonConverter;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnknownTypeException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
        while(udpListenerThread.isAlive());

        JsonConverter jsonConverter = new JsonConverter();

        //Do some processing of the data

        //if no messages received, try a deletion of the node at the NamingServer?
        if(receivedMessages.size() == 0)
            throw new Exception("No mesages received within the timeframe");

        // reformat the list to unique messages
        List<String> filteredMessages = new ArrayList<>();
        for (String message : receivedMessages){
            AMessage m = (AMessage) jsonConverter.toObject(message, AMessage.class);

            //Vraag message id op en vergelijk met de opgeslagen messages.
            long temp = m.getMessageId();
            if(filteredMessages.stream().noneMatch(mes -> mes.matches(String.valueOf(temp)))) {
                filteredMessages.add(message);
            }
        }

        //Zet om naar correcte message
        List<AMessage> messages = new ArrayList<>();
        for(String stringMessage : filteredMessages){
            AMessage temp = (AMessage) jsonConverter.toObject(stringMessage, AMessage.class);

            switch (temp.getMessageType()){
                case UnicastNamingServerToNode -> messages.add((UNAMObject) jsonConverter.toObject(stringMessage, UNAMObject.class));
                case UnicastNodeToNode -> messages.add((UNAMNObject) jsonConverter.toObject(stringMessage, UNAMNObject.class));
                case null, default -> throw new Exception("Class or json not properly formed");
            }

        }

        ClientNode newNode = (ClientNode) node; //Casting

        //Check the amount of nodes present in the network
        UNAMObject temp = (UNAMObject)messages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNamingServerToNode).toList().getFirst();

        if(temp.getAmountOfNodes() > 1){
            //More than 1 so use neighbour nodes its data to form the prev and next node.

            //fetch messages as UNAMNObjects
            List<UNAMNObject> nodeMessages = Collections.singletonList((UNAMNObject) messages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).toList());
            if(nodeMessages.size() <= 1) throw new Exception("Not enough nodes have send out their multicast response!");

            //fetch other data from other nodes.
            if(messages.stream().filter(m -> m.getMessageType() == eMessageTypes.UnicastNodeToNode).count() >1 ) //check if 2 nodes send their info
            {
                newNode.setPrevNodeId(nodeMessages.stream().filter(m -> m.getPrevNodeId() == -1).toList().getFirst().getNodeHashId());//Get the node with lowest ID and add to prevNodeId
                newNode.setNextNodeId(nodeMessages.stream().filter(m -> m.getNextNodeId() == -1).toList().getFirst().getNodeHashId());//Get the node with highest ID and add to prevNodeId
                //Maybe more check before adding node id's?
            }
        }
        else if (temp.getAmountOfNodes() < 1){
            // no nodes in the network so set prev and next node as its own hash ID.
            newNode.setNextNodeId(newNode.getId());
            newNode.setPrevNodeId(newNode.getId());
        }
        return newNode;
    }

    private void udpListener(ServerSocket socket){
        try {
            UDPServer listener = new UDPServer(InetAddress.getLocalHost(), socket.getLocalPort(), 256);

            boolean timeout = false;

            long startTimestamp = System.currentTimeMillis();
            long totalTime = 10000; //10 seconds

            while(!timeout)
            {
                try {
                    receivedMessages.add(listener.listen(500));
                }catch (SocketTimeoutException ex){
                    System.out.println("socket timeout of 500");
                }
                //if the messages are received within a specific time (totalTime seconds)
                if(startTimestamp + totalTime < System.currentTimeMillis())
                {
                    System.out.println("Final timeout");
                    timeout = true;
                }
            }

            listener.close();

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}