package nintendods.ds_project.service;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer {
    private DatagramSocket serverSoc = null;
    private byte[] buffer = null;
    private int bufferSize = 0;

    public UDPServer(InetAddress address, int port, int buffSize) throws SocketException {
        this.serverSoc = new DatagramSocket(port, address);
        this.bufferSize = buffSize;
        this.buffer = new byte[bufferSize];
    }

    public String listen() throws IOException {
        DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);

        System.out.println("Waiting for UDP packet");
        this.serverSoc.receive(packet);

        var data = packet.getData(); //Blocking method

        String text = new String(data, 0, packet.getLength());
        System.out.println(text);
        System.out.println();

        this.serverSoc.close();
        System.out.println("UDP server closed");
        return text;
    }

    public void Close() {
        if (!this.serverSoc.isClosed()) {
            this.serverSoc.close();
        }
    }
}
