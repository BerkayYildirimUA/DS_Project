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
    private int prevID;
    private String nextNodeAddress;
    private int nextID;
    private String namingServerAddress;
    private ScheduledExecutorService executor;
    private volatile boolean running = true;

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityMonitor.class);

    public ConnectivityMonitor(String previousNodeAddress, int prevID, String nextNodeAddress, int nextID, String namingServerAddress) {
        this.previousNodeAddress = previousNodeAddress.substring(1);
        this.nextNodeAddress = nextNodeAddress.substring(1);
        this.namingServerAddress = namingServerAddress;
        this.nextID = nextID;
        this.prevID = prevID;
    }

    public void startMonitoring() {
        executor = Executors.newScheduledThreadPool(3);
        // Schedule ping to previous node, next node, and naming server
        schedulePing(executor, this.previousNodeAddress, String.valueOf(this.prevID));
        schedulePing(executor, this.nextNodeAddress, String.valueOf(this.nextID));
        schedulePing(executor, this.namingServerAddress, "naming server");
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
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
            logger.info("Trying to ping: " + nodeName + "; " + address);
            boolean success = pingNode(address);
            if (success) {
                logger.info("Ping Success: pinged " + nodeName + " successful.");
            } else {
                logger.warn("Failed to connect to " + nodeName + ".");
            }
        }, 0, 10, TimeUnit.SECONDS);  // Ping once every 10 seconds
    }

    private boolean pingNode(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            // Attempt to ping the address with a timeout of 5000 milliseconds
            return inetAddress.isReachable(5000);
        } catch (Exception e) {
            logger.warn("Error pinging node at " + address + ": " + e.getMessage());
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
                        logger.warn("Executor did not terminate");
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
