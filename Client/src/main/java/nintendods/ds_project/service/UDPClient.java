package nintendods.ds_project.service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    private DatagramSocket clientSoc = null;
    private SocketAddress serverSoc = null;
    private byte[] buffer = null;
    private int bufferSize = 0;
    public UDPClient(InetAddress address, int port, int buffSize) throws SocketException {
        this.clientSoc = new DatagramSocket();
        this.serverSoc = new InetSocketAddress(address,port);

        this.bufferSize = buffSize;
        this.buffer = new byte[bufferSize];
    }

    public void SendMessage(String message) throws IOException {
        DatagramPacket packet = null;
        if (message.length() < bufferSize){
            // Send
            buffer = message.getBytes(StandardCharsets.UTF_8);

            packet = new DatagramPacket(this.buffer, this.buffer.length, serverSoc);
            clientSoc.send(packet);
        }
    }

    public void close(){
        if (!this.clientSoc.isClosed()){
            this.clientSoc.close();
        }
    }
}
