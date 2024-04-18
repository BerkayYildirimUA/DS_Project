package nintendods.ds_project.service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * A client for sending UDP packets.
 */
public class UDPClient {
    private DatagramSocket clientSoc = null; // Socket for UDP communication
    private SocketAddress serverSoc = null; // Remote endpoint address and port
    private byte[] buffer = null; // Buffer to hold the data to be sent
    private int bufferSize = 0; // Maximum size of the buffer

    /**
     * Constructs a UDPClient.
     * @param address The IP address of the server to send packets to.
     * @param port The port number on the server to send packets to.
     * @param buffSize The maximum size of the data buffer.
     * @throws SocketException If a socket could not be created.
     */
    public UDPClient(InetAddress address, int port, int buffSize) throws SocketException {
        this.clientSoc = new DatagramSocket(); // Creates a socket bound to any available local port.
        this.serverSoc = new InetSocketAddress(address,port); // Sets the remote address and port.

        this.bufferSize = buffSize;
        this.buffer = new byte[bufferSize];
    }

    /**
     * Sends a message via UDP.
     * @param message The string message to be sent.
     * @throws IOException If an I/O error occurs.
     */
    public void SendMessage(String message) throws IOException {
        DatagramPacket packet = null;
        buffer = null;

        if (message.length() < bufferSize){
            // Send
            buffer = message.getBytes(StandardCharsets.UTF_8); // Converts the message into bytes using UTF-8 encoding.

            packet = new DatagramPacket(this.buffer, this.buffer.length, serverSoc); // Prepares the packet to send.
            clientSoc.send(packet); // Sends the packet.
        }
    }

    /**
     * Closes the UDP socket.
     */
    public void close(){
        if (!this.clientSoc.isClosed()){
            this.clientSoc.close(); // Closes the socket if it is still open.
        }
    }
}
