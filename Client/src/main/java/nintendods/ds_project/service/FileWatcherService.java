package nintendods.ds_project.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import nintendods.ds_project.model.file.WatchObject;
import nintendods.ds_project.model.file.eEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileWatcherService {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);

    private final String directoryToWatch;
    private WatchService watchService;
    private ExecutorService executorService;
    private boolean running;

    private Consumer<WatchObject> fileChangeListener;

    public FileWatcherService(@Value("${file.watcher.directory}") String directoryToWatch) {
        this.directoryToWatch = directoryToWatch;
    }

    public void setFileChangeListener(Consumer<WatchObject> listener) {
        this.fileChangeListener = listener;
    }

    //@PostConstruct
    public void init() {
        Path path = Paths.get(directoryToWatch);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Directory to watch does not exist: " + directoryToWatch);
            System.out.println(path);
            System.out.println(directoryToWatch);
            System.out.println(!Files.isDirectory(path));
            System.out.println(!Files.exists(path));
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

                        if(filePath.toString().equals("local"))
                            break;

                        logger.info("Event kind: " + kind + ". File affected: " + filePath + ".");
                        if (fileChangeListener != null) {

                            if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE))
                                fileChangeListener.accept(new WatchObject(filePath.toFile(), eEvent.CREATE));

                            if(kind.equals(StandardWatchEventKinds.ENTRY_DELETE))
                            fileChangeListener.accept(new WatchObject(filePath.toFile(), eEvent.DELETE));
                            
                            if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
                                fileChangeListener.accept(new WatchObject(filePath.toFile(), eEvent.CHANGE));
                        }
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