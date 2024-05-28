// package nintendods.ds_project.controller;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.mockStatic;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.lang.reflect.Method;
// import java.net.InetAddress;
// import java.net.UnknownHostException;
// import java.util.HashMap;
// import java.util.Map;

// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.MockitoAnnotations;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.client.RestTemplate;
// import org.w3c.dom.Node;

// import nintendods.ds_project.Client;
// import nintendods.ds_project.config.ClientNodeConfig;
// import nintendods.ds_project.model.ANetworkNode;
// import nintendods.ds_project.model.ANode;
// import nintendods.ds_project.model.ClientNode;
// import nintendods.ds_project.model.message.UNAMObject;
// import nintendods.ds_project.service.SyncAgent;
// import nintendods.ds_project.utility.ApiUtil;
// import nintendods.ds_project.utility.Generator;
// import nintendods.ds_project.utility.JsonConverter;

// public class ClientAgentAPITest {
//     @Mock
//     private ConfigurableApplicationContext context;

//     @Mock
//     private Client client;

//     @Mock
//     private ClientNode clientNode;

//     @Mock
//     private UNAMObject mockUNAMObject;

//     @Mock
//     private RestTemplate mockRestTemplate;

//     @InjectMocks
//     private ClientAgentAPI clientAgentAPI;

//     @BeforeEach
//     public void setUp() throws Exception {
//         try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class)) {
//             MockitoAnnotations.openMocks(this);
//             when(context.getBean(Client.class)).thenReturn(client);
//             when(client.getNode()).thenReturn(clientNode);
//             when(clientNode.getNextNodeId()).thenReturn(2);

//             when(mockUNAMObject.getNSAddress()).thenReturn("localhost");
//             ApiUtil.setRestTemplate(mockRestTemplate);
//             ApiUtil.setNsObject(mockUNAMObject);
//             String expectedUrl = "http://localhost:8089/node/2";
//             String expectedResponse = "192.168.1.1";
//             ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
//             when(mockRestTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponseEntity);

//             InetAddress mockInetAddress = mock(InetAddress.class);
//             inetAddressMockedStatic.when(InetAddress::getLocalHost).thenReturn(mockInetAddress);
//             when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");

//         }
//     }

//     @Test
//     public void testRecieveSyncAgent_Success() {

//     }

//     @Test
//     public void testRecieveSyncAgent_Failure() {

//     }

//     @Test
//     public void testProcessSyncAgent() throws Exception {
//         String syncAgentFiles = "{\"file1\":true, \"file2\":false}";
//         Map<String, Boolean> filesMap = new HashMap<>();
//         filesMap.put("file1", true);
//         filesMap.put("file2", false);

//         SyncAgent syncAgent = new SyncAgent(filesMap);
//         when(client.getNode().getNextNodeId()).thenReturn(2);

//         // Access the private method
//         // To test the asynchronous method, we call it directly
//         // clientAgentAPI.processSyncAgent(syncAgentFiles); //Is a private method so do
//         // reflecttion

//         // Access the private method
//         Method method = ClientAgentAPI.class.getDeclaredMethod("processSyncAgent", String.class);
//         method.setAccessible(true);
//         method.invoke(clientAgentAPI, syncAgentFiles);

//         verify(client, times(2)).getNode();
//         // verify(ApiUtil.class, times(1));
//         // ApiUtil.sendSyncAgent("127.0.0.1", ClientNodeConfig.API_PORT, syncAgent);
//     }
// }
