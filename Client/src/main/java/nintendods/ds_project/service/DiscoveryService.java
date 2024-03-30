package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.message.*;
import nintendods.ds_project.utility.JsonConverter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryService {
    private List<String> receivedMessages;
    private String multicastAddress = "223.0.0.100";
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

    public UNAMObject discover(ABaseNode node) throws IOException {
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
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, socket.getInetAddress().getHostAddress(),socket.getLocalPort(), node.getName()));
        ms.multicastSend(new MNObject(udp_id, eMessageTypes.MulticastNode, socket.getInetAddress().getHostAddress(),socket.getLocalPort(), node.getName()));

        //Wait for UDP packet to be filled in.
        while(udpListenerThread.isAlive());

        JsonConverter jsonConverter = new JsonConverter();

        //Do some processing of the data
        // reformat the list to unique messages
        List<String> filteredMessages = new ArrayList<>();
        for (String message : receivedMessages){
            AMessage m = (AMessage) jsonConverter.toObject(message, AMessage.class);

            long temp = m.getMessageId();
            if(filteredMessages.stream().noneMatch(mes -> mes.matches(String.valueOf(temp)))) {
                filteredMessages.add(message);
            }
        }

        //TODO: nog omzetten naar de message en dan instellen a.d.h.v. de amount of nodes present in het netwerk.
        UNAMObject receiveData = (UNAMObject) jsonConverter.toObject(receivedMessage, UNAMObject.class);

        return receiveData;
    }

    private void udpListener(ServerSocket socket){
        try {
            UDPServer listener = new UDPServer(socket.getInetAddress(), socket.getLocalPort(), 256);

            boolean timeout = false;

            long startTimestamp = System.currentTimeMillis();
            long totalTime = 10000; //10 seconds

            while(!timeout)
            {
                receivedMessages.add(listener.listen());

                //if the messages are received within a specific time (totalTime seconds)
                if(startTimestamp + totalTime > System.currentTimeMillis())
                {
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