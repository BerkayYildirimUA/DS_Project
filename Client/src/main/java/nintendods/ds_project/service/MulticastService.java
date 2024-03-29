package nintendods.ds_project.service;

import nintendods.ds_project.helper.JsonConverter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastService {
    private DatagramSocket socket;
    private InetAddress group;
    private int port;
    private byte[] buf;

    public MulticastService(String multicastAddress, int port ) throws UnknownHostException{
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