package nintendods.ds_project.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Component
public class MulticastListener extends Thread {
    private MulticastSocket socket = null;
    private byte[] buf = new byte[256];

    public void run() {
        try {
            socket = new MulticastSocket(12345);
            InetAddress group = null;
            group = InetAddress.getByName("224.0.0.100");
            socket.joinGroup(group);
            System.out.println("Listening for multicasts");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(
                        packet.getData(), 0, packet.getLength());
                System.out.println(received);
                if ("end".equals(received)) {
                    break;
                }
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}