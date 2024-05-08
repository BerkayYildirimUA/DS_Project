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

    public ConnectivityMonitor() {
        this.previousNodeAddress = "0.0.0.0";
        this.nextNodeAddress = "0.0.0.0";
        this.namingServerAddress = "0.0.0.0";
    }

    public void startMonitoring(String previousNodeAddress, String nextNodeAddress, String namingServerAddress) {
        setNextNodeAddress(nextNodeAddress);
        setPreviousNodeAddress(previousNodeAddress);
        setNamingServerAddress(namingServerAddress);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

        // Schedule ping to previous node
        executor.scheduleWithFixedDelay(() -> {
            if (!pingNode(previousNodeAddress)) {
                throw new RuntimeException("Failed to connect to previous node.");
            }
        }, 0, 10, TimeUnit.SECONDS);

        // Schedule ping to next node
        executor.scheduleWithFixedDelay(() -> {
            if (!pingNode(nextNodeAddress)) {
                throw new RuntimeException("Failed to connect to next node.");
            }
        }, 0, 10, TimeUnit.SECONDS);

        // Schedule ping to naming server
        executor.scheduleWithFixedDelay(() -> {
            if (!pingNode(namingServerAddress)) {
                throw new RuntimeException("Failed to connect to naming server.");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private boolean pingNode(String address) {
        try (Socket socket = new Socket(address, 2222); // Replace PORT with the actual port number
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send a ping message
            out.println("PING");
            // Wait for a response
            String response = in.readLine();

            // Check if the response is as expected
            return "PONG".equals(response);
        } catch (Exception e) {
            System.out.println("Error pinging node at " + address + ": " + e.getMessage());
            return false;
        }
    }

    public void setNextNodeAddress(String address) {
        this.nextNodeAddress = nextNodeAddress;
    }

    public void setPreviousNodeAddress(String address) {
        this.previousNodeAddress = nextNodeAddress;
    }

    public void setNamingServerAddress(String address) {
        this.namingServerAddress = namingServerAddress;
    }
}
