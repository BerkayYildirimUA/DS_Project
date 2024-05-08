package nintendods.ds_project.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

        System.out.println("next:" + this.nextNodeAddress);
        System.out.println("prev:" + this.previousNodeAddress);
        System.out.println("nameserver:" + this.namingServerAddress);

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
        }, 0, 2, TimeUnit.SECONDS); // Ping twice every 5 seconds
    }

    private boolean pingNode(String address, int port) {
        try (Socket socket = new Socket(address, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PING");
            String response = in.readLine();

            return "PONG".equals(response);
        } catch (Exception e) {
            System.out.println("Error pinging node at " + address + ": " + e.getMessage());
            return false;
        }
    }

    public void setNextNodeAddress(String address) {
        this.nextNodeAddress = address;
    }

    public void setPreviousNodeAddress(String address) {
        this.previousNodeAddress = address;
    }

    public void setNamingServerAddress(String address) {
        this.namingServerAddress = address;
    }
}
