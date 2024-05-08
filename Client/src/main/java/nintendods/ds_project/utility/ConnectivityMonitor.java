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
    private ScheduledExecutorService executor;
    private volatile boolean running = true;

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityMonitor.class);

    public ConnectivityMonitor(String previousNodeAddress, String nextNodeAddress, String namingServerAddress) {
        this.previousNodeAddress = previousNodeAddress.substring(1);
        this.nextNodeAddress = nextNodeAddress.substring(1);
        this.namingServerAddress = namingServerAddress;
    }

    public void startMonitoring() {
        executor = Executors.newScheduledThreadPool(3);
        // Schedule ping to previous node, next node, and naming server
        schedulePing(executor, this.previousNodeAddress, "previous node");
        schedulePing(executor, this.nextNodeAddress, "next node");
        schedulePing(executor, this.namingServerAddress, "naming server");
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.info("Couldn't wait for 10 seconds");
        }
    }

    private void schedulePing(ScheduledExecutorService executor, String address, String nodeName) {
        executor.scheduleAtFixedRate(() -> {
            if (!running) {
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("Trying to ping: " + nodeName + "; " + address);
            boolean success = pingNode(address);
            if (success) {
                System.out.println("Success: Connection to " + nodeName + " successful.");
            } else {
                System.out.println("Failed to connect to " + nodeName + ".");
            }
        }, 0, 10, TimeUnit.SECONDS);  // Ping once every 10 seconds
    }

    private boolean pingNode(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            // Attempt to ping the address with a timeout of 5000 milliseconds
            return inetAddress.isReachable(5000);
        } catch (Exception e) {
            System.out.println("Error pinging node at " + address + ": " + e.getMessage());
            return false;
        }
    }

    public void stop() {
        running = false;  // Signal all running threads to stop.
        if (executor != null) {
            executor.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Executor did not terminate");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }
}
