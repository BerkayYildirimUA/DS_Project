package nintendods.ds_project.utility;

import nintendods.ds_project.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectivityMonitor {
    private String previousNodeAddress;
    private String nextNodeAddress;
    private String namingServerAddress;

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityMonitor.class);

    public ConnectivityMonitor(String previousNodeAddress, String nextNodeAddress, String namingServerAddress) {
        this.previousNodeAddress = previousNodeAddress.substring(1);
        this.nextNodeAddress = nextNodeAddress.substring(1);
        this.namingServerAddress = namingServerAddress;
    }

    public void startMonitoring() throws IOException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

        // Schedule ping to previous node, next node, and naming server
        schedulePing(executor, this.previousNodeAddress, "previous node");
        schedulePing(executor, this.nextNodeAddress, "next node");
        schedulePing(executor, this.namingServerAddress, "naming server");
    }

    private void schedulePing(ScheduledExecutorService executor, String address, String nodeName) {
        executor.scheduleAtFixedRate(() -> {
            boolean success = pingNode(address);
            if (!success) {
                logger.info("Failed to connect to " + nodeName + ".");
            }
        }, 0, 10, TimeUnit.SECONDS);  // Ping once every 10 seconds
    }

    private boolean pingNode(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            // Attempt to ping the address with a timeout of 5000 milliseconds
            return inetAddress.isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }

    public void stop() {
        // Include any cleanup or shutdown procedures if necessary
    }
}
