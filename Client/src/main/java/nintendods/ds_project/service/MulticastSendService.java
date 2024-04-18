package nintendods.ds_project.service;

import nintendods.ds_project.utility.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class MulticastSendService {
    private DatagramSocket socket;
    private InetAddress group;
    private int port;
    private byte[] buf;

    // Constructor that initializes the multicast group and port
    public MulticastSendService(@Value("${udp.multicast.address}") String multicastAddress,
                                @Value("${udp.multicast.port}") int port ) throws UnknownHostException{
        this.group = InetAddress.getByName(multicastAddress); // Convert the string address to an InetAddress
        this.port = port;
        // TODO: put in try catch block?
    }

    // Sends a multicast message to the group
    public void multicastSend(Object multicastObject) throws IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String jsonText = jsonConverter.toJson(multicastObject); // Convert the object to JSON string

        socket = new DatagramSocket(); // Open a new DatagramSocket
        buf = jsonText.getBytes(); // Convert the string into bytes

        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port); // Create a packet with the byte array, length, group, and port
        socket.send(packet); // Send the packet
        socket.close(); // Close the socket
    }
}