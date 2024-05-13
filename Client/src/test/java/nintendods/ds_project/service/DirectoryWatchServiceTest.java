package nintendods.ds_project.service;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import nintendods.ds_project.model.ClientNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DirectoryWatchServiceTest {
    @Mock
    private WatchService mockWatchService;
    @Mock
    private WatchKey mockWatchKey;
    @Mock
    private ClientNode mockNode;
    @Mock
    private FileDBService mockFileDBService;
    private DirectoryWatchService service = new DirectoryWatchService("/local", mockNode);

    private Path directory = Paths.get("/local");

    public DirectoryWatchServiceTest() throws IOException {
    }

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(mockWatchService.take()).thenReturn(mockWatchKey);
        when(directory.register(eq(mockWatchService), any())).thenReturn(mockWatchKey);
        when(mockWatchKey.pollEvents()).thenReturn(new ArrayList<>()); // No events initially

        // Initialize DirectoryWatchService with the mocked path and node
        service = new DirectoryWatchService(directory.toString(), mockNode);
    }

    @Test
    void testProcessEventHandling() throws InterruptedException {
        // Simulate a file creation event
        WatchEvent<Path> creationEvent = new WatchEventStub<>(StandardWatchEventKinds.ENTRY_CREATE, directory.resolve("newfile.txt"));
        when(mockWatchKey.pollEvents()).thenReturn(List.of(creationEvent));

        service.processEvents(); // Process the mock event

        // Verify the interaction with FileDBService
        verify(mockFileDBService).getFileDB().addOrUpdateFile(anyString(), anyString());
    }

    @Test
    void testServiceShutdown() throws IOException {
        service.shutdown();
        verify(mockWatchService).close();
    }

    @Test
    void testServiceHandlesInterruptedException() throws InterruptedException {
        when(mockWatchService.take()).thenThrow(InterruptedException.class);

        service.processEvents();

        verify(mockWatchKey, never()).reset();
        assertTrue(Thread.interrupted()); // Check if the thread's interrupt flag was set
    }

    private static class WatchEventStub<T> implements WatchEvent<T> {
        private final Kind<T> kind;
        private final T context;

        WatchEventStub(Kind<T> kind, T context) {
            this.kind = kind;
            this.context = context;
        }

        @Override
        public Kind<T> kind() {
            return kind;
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public T context() {
            return context;
        }
    }
}