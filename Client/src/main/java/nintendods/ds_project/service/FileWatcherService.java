package nintendods.ds_project.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;

import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileWatcherService {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);

    private final String directoryToWatch;
    private WatchService watchService;
    private ExecutorService executorService;
    private boolean running;

    public FileWatcherService(@Value("${file.watcher.directory}") String directoryToWatch) {
        this.directoryToWatch = directoryToWatch;
    }

    @PostConstruct
    public void init() {
        Path path = Paths.get(directoryToWatch);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Directory to watch does not exist: " + directoryToWatch);
            return;
        }

        executorService = Executors.newSingleThreadExecutor();
        startWatching();
    }

    public void startWatching() {
        executorService.submit(() -> {
            running = true;
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(directoryToWatch);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                while (running) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        if (!running) {
                            break;
                        }
                        Thread.currentThread().interrupt();
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path filePath = (Path) event.context();
                        logger.info("Event kind: " + kind + ". File affected: " + filePath + ".");
                        // Here you can add the logic to replicate the file changes.
                    }
                    key.reset();
                }
            } catch (IOException e) {
                logger.error("Error watching directory: " + directoryToWatch, e);
            }
        });
    }

    @PreDestroy
    public void stopWatching() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.error("Error closing WatchService", e);
            }
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
