package nintendods.ds_project.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

import org.springframework.beans.factory.annotation.Value;
@Service
public class FileWatcherService {

    private final String directoryToWatch;

    public FileWatcherService(@Value("${file.watcher.directory}") String directoryToWatch) {
        this.directoryToWatch = directoryToWatch;
    }

    @PostConstruct
    public void init() {
        startWatching();
    }

    public void startWatching() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(directoryToWatch);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = (Path) event.context();
                    System.out.println("Event kind: " + kind + ". File affected: " + filePath + ".");
                    // Here you can add the logic to replicate the file changes.
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();  // Replace with proper logging
        }
    }
}
