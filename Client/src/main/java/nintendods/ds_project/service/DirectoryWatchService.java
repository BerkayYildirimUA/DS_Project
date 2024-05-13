package nintendods.ds_project.service;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.utility.Generator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static nintendods.ds_project.config.ClientNodeConfig.NODE_GLOBAL_PORT;
import static nintendods.ds_project.config.ClientNodeConfig.NODE_NAME_LENGTH;

import java.nio.file.attribute.*;
import java.io.IOException;
import java.util.*;

public class DirectoryWatchService {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final ClientNode node;
    private volatile boolean running = true; // flag to control the loop

    public DirectoryWatchService(String dir_name, ClientNode node) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.node = node;
        Path dir = Paths.get(dir_name);
        registerDirectory(dir);
    }

    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    public void processEvents() {
        while (running) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                Thread.currentThread().interrupt(); // restore interrupt status
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                System.out.format("%s: %s\n", event.kind().name(), child);

                if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                    FileDBService.getFileDB().addOrUpdateFile(child.toString(), String.valueOf(node.getAddress()));
                } else if (kind == ENTRY_DELETE) {
                    FileDBService.getFileDB().removeFile(child.toString());
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    public void shutdown() {
        running = false; // Set the running flag to false to stop the processing loop
        try {
            watcher.close(); // Close the WatchService to free up resources
        } catch (IOException e) {
            System.err.println("Error closing watcher: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public static void main(String[] args) throws IOException {
        ClientNode node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, Generator.randomString(NODE_NAME_LENGTH));
        DirectoryWatchService service = new DirectoryWatchService("/local", node);
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdown)); // Ensure service is cleaned up on JVM shutdown
        service.processEvents();
    }
}
