package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.utility.JsonConverter;

import java.io.IOException;
import java.net.*;

public class DiscoveryService {

    private String receivedMessage;
    private String multicastAddress = "223.0.0.100";
    private int multicastPort = 12345;

    /**
     * Create a Discovery service object.
     * Will send out a discovery message with format {@link MNObject} and waits for a response of the Naming server of type {@link UNAMObject}.
     */
    public DiscoveryService() {
        //Setup the socket and get the port
        this.receivedMessage = "";
    }
    public DiscoveryService(String multicastAddress, int multicastPort) {
        //Setup the socket and get the port
        this.receivedMessage = "";
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    public UNAMObject discover(ABaseNode node) throws IOException {
        //Create server socket where we'll listen on when the multicast is sended out.
        ServerSocket socket = new ServerSocket(0);

        //Set up the UDPServer and set it in a thread
        Thread udpListenerThread = new Thread(() -> udpListener(socket));
        udpListenerThread.start();

        //Send out the multicast
        MulticastService ms = new MulticastService(multicastAddress, multicastPort);
        long udp_id = System.currentTimeMillis(); // unique messageID
        ms.multicastSend(new MNObject(udp_id, socket.getInetAddress().getHostAddress(),socket.getLocalPort(), node.getName()));
        ms.multicastSend(new MNObject(udp_id, socket.getInetAddress().getHostAddress(),socket.getLocalPort(), node.getName()));

        //Wait for UDP packet to be filled in.
        while(udpListenerThread.isAlive());

        JsonConverter jsonConverter = new JsonConverter();
        UNAMObject receiveData = (UNAMObject) jsonConverter.toObject(receivedMessage, UNAMObject.class);

        return receiveData;
    }

    private void udpListener(ServerSocket socket){
        //Setup

        try {
            UDPServer listener= new UDPServer(socket.getInetAddress(), socket.getLocalPort(), 256);
            receivedMessage = listener.listen();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
