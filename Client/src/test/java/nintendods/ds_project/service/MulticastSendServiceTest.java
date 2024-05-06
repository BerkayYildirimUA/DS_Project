package nintendods.ds_project.service;

import nintendods.ds_project.utility.JsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MulticastSendServiceTest {

    @Mock
    private DatagramSocket socket;

    @Mock
    private JsonConverter jsonConverter;

    private MulticastSendService service;

    @Captor
    private ArgumentCaptor<DatagramPacket> packetCaptor;

    private InetAddress group;
    private final int port = 4446;

    @BeforeEach
    public void setUp() throws Exception {
        group = InetAddress.getByName("224.0.0.1"); // Using a common multicast address
        service = new MulticastSendService("224.0.0.1", port);
        service.socket = socket; // Set the mocked socket directly into the service
    }

    @Test
    void multicastSend_sendsPacket() throws Exception {
        Object multicastObject = new Object();
        String jsonText = "{\"key\": \"value\"}";
        when(jsonConverter.toJson(multicastObject)).thenReturn(jsonText);

        service.multicastSend(multicastObject);

        byte[] buf = jsonText.getBytes();

        verify(socket).send(packetCaptor.capture()); // Use the ArgumentCaptor to capture the packet
        DatagramPacket capturedPacket = packetCaptor.getValue();

        assertArrayEquals(buf, capturedPacket.getData());
        assertEquals(group, capturedPacket.getAddress());
        assertEquals(port, capturedPacket.getPort());

        verify(socket).close();
    }
}
