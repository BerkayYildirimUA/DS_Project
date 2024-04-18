package nintendods.ds_project.service;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A server for receiving UDP packets.
 */
public class UDPServer {
    private DatagramSocket serverSoc = null; // Socket for UDP communication
    private byte[] buffer = null; // Buffer to store incoming data
    private int bufferSize = 1024; // Size of the buffer
    private final DatagramPacket packet; // Predefined packet to receive data

    /**
     * Constructs a UDPServer to receive packets.
     * @param address The local IP address to bind the server socket.
     * @param port The local port to bind the server socket.
     * @param buffSize The size of the buffer for receiving data.
     * @throws SocketException If a socket could not be opened.
     */
    public UDPServer(InetAddress address, int port, int buffSize) throws SocketException {
        this.serverSoc = new DatagramSocket(port, address); // Binds the socket to a specific local port and IP address.
        this.bufferSize = buffSize;
        this.buffer = new byte[bufferSize];
        this.packet = new DatagramPacket(this.buffer, this.buffer.length); // Initializes the packet with the buffer.
        serverSoc.setSoTimeout(500); // Sets a timeout for blocking receive calls.
        serverSoc.setReuseAddress(true); // Allows the socket to be bound even though a previous connection is still in a timeout state.
    }

    /**
     * Listens for an incoming UDP packet.
     * @param timeout The timeout in milliseconds to wait for a packet.
     * @return The text received from the packet.
     * @throws IOException If an I/O error occurs during receiving.
     */
    public String listen(int timeout) throws IOException {
        this.serverSoc.receive(packet); // Receives a packet.
        var data = packet.getData(); //Blocking method
        String text = new String(data, 0, packet.getLength()); // Converts the received data into a string.
        //System.out.println(text);
        return text;
    }

    /**
     * Closes the server socket.
     */
    public void close() {
        if (!this.serverSoc.isClosed()) {
            this.serverSoc.close(); // Closes the socket if it is still open.
        }
    }
}
