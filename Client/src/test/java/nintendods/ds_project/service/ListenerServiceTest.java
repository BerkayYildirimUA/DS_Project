package nintendods.ds_project.service;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.eMessageTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListenerServiceTest {

    @Mock
    private MulticastListenService mockMulticastService;

    @Mock
    private ClientNode mockNode;

    private MulticastListenerService listenerService;

    private final String multicastAddress = "224.0.0.1";
    private final int multicastPort = 4446;
    private final int multicastBufferCapacity = 10;

    @BeforeEach
    void setUp() {
        listenerService = new MulticastListenerService(multicastAddress, multicastPort, multicastBufferCapacity);
        listenerService.multicastService = mockMulticastService;
    }

    @Test
    void listenAndUpdate_test1() throws Exception {
        MNObject testMessage = new MNObject(1, eMessageTypes.MulticastNode, "127.0.0.1", 20000, "testNode");
        ClientNode incomingNode = new ClientNode(testMessage);

        when(mockNode.getId()).thenReturn(10);
        when(mockNode.getNextNodeId()).thenReturn(20);
        when(mockNode.getPrevNodeId()).thenReturn(5);

        when(mockMulticastService.getMessage()).thenReturn(testMessage);
        when(mockNode.getId()).thenReturn(incomingNode.getId() + 1);

        listenerService.listenAndUpdate(mockNode);

        verify(mockNode, times(4)).getId();
    }

    @Test
    void listenAndUpdate_test2() throws Exception {
        when(mockMulticastService.getMessage()).thenReturn(null);

        listenerService.listenAndUpdate(mockNode);

        verify(mockNode, never()).setNextNodeId(anyInt());
    }

    @Test
    void initializeMulticast_test() {
        listenerService.initialize_multicast();
        verify(mockMulticastService, times(1)).initialize();
    }
}
