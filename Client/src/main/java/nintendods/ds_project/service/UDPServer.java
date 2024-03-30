package nintendods.ds_project.service;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer {
    private DatagramSocket serverSoc = null;
    private byte[] buffer = null;
    private int bufferSize = 256;
    private final DatagramPacket packet;

    public UDPServer(InetAddress address, int port, int buffSize) throws SocketException {
        this.serverSoc = new DatagramSocket(port, address);
        this.bufferSize = buffSize;
        this.buffer = new byte[bufferSize];
        this.packet = new DatagramPacket(this.buffer, this.buffer.length);
    }

    public String listen() throws IOException {
        this.serverSoc.receive(packet);
        var data = packet.getData(); //Blocking method
        return new String(data, 0, packet.getLength());
    }

    public void close() {
        if (!this.serverSoc.isClosed()) {
            this.serverSoc.close();
        }
    }
}
