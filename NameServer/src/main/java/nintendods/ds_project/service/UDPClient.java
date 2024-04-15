package nintendods.ds_project.service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Manages UDP communication, sending data to a specified network address.
 */
public class UDPClient {
    private DatagramSocket clientSoc = null;
    private SocketAddress serverSoc = null;
    private byte[] buffer = null;
    private int bufferSize = 0;

    /**
     * Initializes the UDP client with a specific destination address and port.
     * @param address The destination address.
     * @param port The destination port.
     * @param buffSize The size of the send buffer.
     * @throws SocketException if the socket could not be opened.
     */
    public UDPClient(InetAddress address,int port, int buffSize) throws SocketException {
        this.clientSoc = new DatagramSocket();
        this.serverSoc = new InetSocketAddress(address,port);

        this.bufferSize = buffSize;
        this.buffer = new byte[bufferSize];
    }

    /**
     * Sends a message to the configured address and port.
     * @param message The message to send.
     * @throws IOException if there is an error during sending.
     */
    public void SendMessage(String message) throws IOException {
        DatagramPacket packet = null;
        if (message.length() < bufferSize){
            // Send
            buffer = message.getBytes(StandardCharsets.UTF_8);

            packet = new DatagramPacket(this.buffer, this.buffer.length, serverSoc);
            clientSoc.send(packet);
        }
    }

    /**
     * Closes the UDP client socket.
     */
    public void close(){
        if (!this.clientSoc.isClosed()){
            this.clientSoc.close();
        }
    }
}
