package nintendods.ds_project.utility;

import nintendods.ds_project.Client;
import nintendods.ds_project.controller.ClientManagementAPI;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TestAPIUtils {

    @Autowired
    ConfigurableApplicationContext context;


    @Test
    void t_Client_PUT_changeMyPrevNodesNeighbor() throws InterruptedException {

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ClientManagementAPI clientManagementAPI = context.getBean(ClientManagementAPI.class);


        doAnswer((Answer<Void>) invocation -> {
            String url = invocation.getArgument(0, String.class);
            int lastSlashIndex = url.lastIndexOf('/');
            String numberPart = url.substring(lastSlashIndex + 1);
            String digits = numberPart.replaceAll("[^\\d]", "");
            int id = Integer.parseInt(digits);

            clientManagementAPI.changePrevNode(id);
            return null;
        }).when(mockRestTemplate).put(anyString(), eq(String.class));

        when(mockRestTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok("127.0.0.1"));

        ApiUtil.setRestTemplate(mockRestTemplate);


        // Arrange
        Client client = context.getBean(Client.class);
        int nodeId = client.getNode().getId();
        int targetPrevNodeId = 100;

        ApiUtil.Client_PUT_changeMyPrevNodesNeighbor(nodeId, 8083, targetPrevNodeId);

        // Assuming you have a way to verify the effect of the PUT operation
        // You might need to adjust this depending on what Client_PUT_changeMyPrevNodesNeighbor does
        assertEquals(targetPrevNodeId, client.getNode().getPrevNodeId());

    }


}
