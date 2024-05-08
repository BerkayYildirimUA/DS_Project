package nintendods.ds_project.utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectivityMonitor {
    private String previousNodeAddress;
    private String nextNodeAddress;
    private String namingServerAddress;

    public ConnectivityMonitor(String previousNodeAddress, String nextNodeAddress, String namingServerAddress) {
        this.previousNodeAddress = previousNodeAddress.substring(1);
        this.nextNodeAddress = nextNodeAddress.substring(1);
        this.namingServerAddress = namingServerAddress;
    }

    public void startMonitoring() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

        // Schedule ping to previous node, next node, and naming server
        schedulePing(executor, this.previousNodeAddress, 8083, "previous node");
        schedulePing(executor, this.nextNodeAddress, 8083, "next node");
        schedulePing(executor, this.namingServerAddress, 8089, "naming server");
    }

    private void schedulePing(ScheduledExecutorService executor, String address, int port, String nodeName) {
        executor.scheduleAtFixedRate(() -> {
            boolean success = pingNode(address, port);
            if (success) {
                System.out.println("Success: Connection to " + nodeName + " successful.");
            } else {
                System.out.println("Failed to connect to " + nodeName + ".");
                throw new RuntimeException("Failed to connect to " + nodeName + ".");
            }
        }, 0, 20, TimeUnit.SECONDS); // Ping twice every 5 seconds
    }

    private boolean pingNode(String address, int port) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(10000); // 5 seconds timeout
            InetAddress inetAddress = InetAddress.getByName(address);

            String message = "PING";
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddress, port);

            // Send the ping
            socket.send(packet);

            // Prepare to receive the pong
            byte[] buf2 = new byte[256];
            DatagramPacket packet2 = new DatagramPacket(buf2, buf2.length);
            socket.receive(packet2);
            String received = new String(packet2.getData(), 0, packet2.getLength());

            socket.close();
            return "PONG".equals(received.trim());
        } catch (Exception e) {
            System.out.println("Error pinging node at " + address + ": " + e.getMessage());
            return false;
        }
    }
}
