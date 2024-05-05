package nintendods.ds_project.service;

import nintendods.ds_project.utility.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component("Send1")
public class MulticastSendService {
    private DatagramSocket socket;
    private InetAddress group;
    private int port;
    private byte[] buf;
    public MulticastSendService(@Value("${udp.multicast.address}") String multicastAddress,
                                @Value("${udp.multicast.port}") int port ) throws UnknownHostException{
        this.group = InetAddress.getByName(multicastAddress);
        this.port = port;
    }

    public void multicastSend(Object multicastObject) throws IOException {
        JsonConverter jsonConverter = new JsonConverter();
        String jsonText = jsonConverter.toJson(multicastObject);

        socket = new DatagramSocket();
        buf = jsonText.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
        socket.send(packet);
        socket.close();
    }
}