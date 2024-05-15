package nintendods.ds_project.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileWatcherServiceTest {

    private static final String DIRECTORY_TO_WATCH = "testDir";
    private Path testDir;
    private FileWatcherService fileWatcherService;
    private ExecutorService executorService;

    @BeforeEach
    public void setUp() throws IOException {
        testDir = Files.createTempDirectory(DIRECTORY_TO_WATCH);
        fileWatcherService = new FileWatcherService(testDir.toString());
        executorService = Executors.newSingleThreadExecutor();

        // Run the watcher service in a separate thread
        executorService.submit(() -> {
            fileWatcherService.startWatching();
        });
    }

    @AfterEach
    public void tearDown() throws IOException {
        executorService.shutdownNow();

        try (var paths = Files.walk(testDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }

    @Test
    public void testFileCreation() throws IOException, InterruptedException {
        Path testFile = testDir.resolve("testFile.txt");
        Files.createFile(testFile);

        // Give some time for the watcher to detect the event
        TimeUnit.SECONDS.sleep(2);

        assertTrue(Files.exists(testFile), "Test file should be created");
    }

    @Test
    public void testFileModification() throws IOException, InterruptedException {
        Path testFile = testDir.resolve("testFile.txt");
        Files.createFile(testFile);
        Files.write(testFile, "Initial Content".getBytes());

        // Modify the file
        Files.write(testFile, "Modified Content".getBytes());

        // Give some time for the watcher to detect the event
        TimeUnit.SECONDS.sleep(2);

        assertTrue(Files.exists(testFile), "Test file should exist after modification");
    }

    @Test
    public void testFileDeletion() throws IOException, InterruptedException {
        Path testFile = testDir.resolve("testFile.txt");
        Files.createFile(testFile);

        // Give some time for the watcher to detect the event
        TimeUnit.SECONDS.sleep(2);

        Files.delete(testFile);

        // Give some time for the watcher to detect the event
        TimeUnit.SECONDS.sleep(2);

        assertTrue(Files.notExists(testFile), "Test file should be deleted");
    }
}
