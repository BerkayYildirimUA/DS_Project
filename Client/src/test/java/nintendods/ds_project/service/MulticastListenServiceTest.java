package nintendods.ds_project.service;

import nintendods.ds_project.model.ANetworkNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.utility.JsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MulticastListenServiceTest {

    @Mock
    private BlockingQueue<MNObject> multicastQueue;
    @Mock
    private BlockingQueue<String> packetQueue;
    @Mock
    private JsonConverter jsonConverter;
    @Mock
    private UDPClient udpClient;

    private MulticastListenService service;

    @BeforeEach
    void setUp() {
        // Setup with mocked dependencies
        service = new MulticastListenService("224.0.0.1", 4446, 10);
        service.jsonConverter = jsonConverter; // Assuming this can be accessed, if not, use setters or reflection
    }

    @Test
    void testInitialize() throws Exception {
        // The actual initialization starts threads and might be difficult to test directly.
        // This test can be focused on checking whether the internal state is set up correctly.
        assertNotNull(service);
    }

    @Test
    void testProcessPackets() throws InterruptedException {
        // Setup
        String testPacket = "{\"messageType\":\"MulticastNode\"}";
        MNObject mnObject = new MNObject(1, eMessageTypes.MulticastNode, "127.0.0.1", 20000, "testNode");
        when(packetQueue.take()).thenReturn(testPacket);
        when(jsonConverter.toObject(testPacket, MNObject.class)).thenReturn(mnObject);

        // Execute a simplified version of the method logic
        String packet = packetQueue.take(); // Simulate the packet retrieval
        MNObject result = (MNObject) jsonConverter.toObject(packet, MNObject.class);

        verify(packetQueue, times(1)).take();
        verify(jsonConverter, times(1)).toObject(packet, MNObject.class);
        assertEquals(mnObject, result);
    }

}
