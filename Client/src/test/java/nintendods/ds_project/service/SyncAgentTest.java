// package nintendods.ds_project.service;

// import static org.junit.Assert.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;

// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.MockitoAnnotations;
// import org.mockito.verification.VerificationMode;
// import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.client.RestTemplate;

// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.mockStatic;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.net.InetAddress;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.MockedStatic;

// import nintendods.ds_project.Client;
// import nintendods.ds_project.config.ClientNodeConfig;
// import nintendods.ds_project.database.FileControl;
// import nintendods.ds_project.model.ClientNode;
// import nintendods.ds_project.model.file.AFile;
// import nintendods.ds_project.model.message.UNAMObject;
// import nintendods.ds_project.utility.ApiUtil;
// import nintendods.ds_project.utility.Generator;
// import nintendods.ds_project.utility.JsonConverter;

// public class SyncAgentTest {
    
//     SyncAgent syncAgent;

//     @Mock
//     private ConfigurableApplicationContext context;

//     @Mock
//     private Client client;

//     @Mock
//     private ClientNode clientNode;

//     @Mock
//     private RestTemplate mockRestTemplate;

//     @Mock
//     private UNAMObject mockUNAMObject;

//     //Do something before running a test
//     @BeforeEach
//     public void setUp() {
//         MockitoAnnotations.openMocks(this);
//         Map<String, Boolean> files = new HashMap<String, Boolean>();
//         files.put("file1", false);
//         files.put("file2", true);
        

//         when(context.getBean(Client.class)).thenReturn(client);
//         when(client.getNode()).thenReturn(clientNode);
//         when(clientNode.getNextNodeId()).thenReturn(2);

//         when(client.getNode()).thenReturn(clientNode);
//         when(clientNode.getNextNodeId()).thenReturn(2);

//         when(mockUNAMObject.getNSAddress()).thenReturn("localhost");
//         ApiUtil.setRestTemplate(mockRestTemplate);
//         ApiUtil.setNsObject(mockUNAMObject);
//         String expectedUrl = "http://localhost:8089/node/2";
//         String expectedResponse = "192.168.1.1";
//         ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
//         when(mockRestTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponseEntity);

//         syncAgent = new SyncAgent(files, context);
//     }

//     @Test
//     public void testSyncFiles() {

//         try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class);
//              MockedStatic<ApiUtil> apiUtilMockedStatic = mockStatic(ApiUtil.class);
//              MockedStatic<JsonConverter> jsonConverterMockedStatic = mockStatic(JsonConverter.class)) {

//             InetAddress mockInetAddress = mock(InetAddress.class);
//             inetAddressMockedStatic.when(InetAddress::getLocalHost).thenReturn(mockInetAddress);
//             when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
            
//             List<AFile> filesOnNode = new ArrayList<>();
//             ClientNode node1 = new ClientNode(InetAddress.getLocalHost(), 21, Generator.randomString(10));
        
//             AFile file1 = new AFile("file1","file1", node1);
//             AFile file3 = new AFile("file3","file3",node1);

        
//             filesOnNode.add(file1);
//             filesOnNode.add(file3);

//             apiUtilMockedStatic.when(() -> ApiUtil.clientGetAllFiles(anyString(), eq(ClientNodeConfig.API_PORT)))
//                     .thenReturn("[{\"name\":\"file1\", \"isReplicated\":false}, {\"name\":\"file3\", \"isReplicated\":false}]");

//             syncAgent.run();

//             Map<String, Boolean> expectedFiles = new HashMap<>();
//             expectedFiles.put("file1", false);
//             expectedFiles.put("file2", true);
//             expectedFiles.put("file3", false);

//             assertEquals(expectedFiles, syncAgent.getFiles());
//         }
//         catch(Exception ex){
//             assertFalse(true);
//         }
//     }

//     @Test
//     public void testProcessLockRequest() {
//         try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class);
//              MockedStatic<ApiUtil> apiUtilMockedStatic = mockStatic(ApiUtil.class);
//              MockedStatic<JsonConverter> jsonConverterMockedStatic = mockStatic(JsonConverter.class)) {

//             InetAddress mockInetAddress = mock(InetAddress.class);
//             inetAddressMockedStatic.when(InetAddress::getLocalHost).thenReturn(mockInetAddress);
//             when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
            
//             List<AFile> filesOnNode = new ArrayList<>();
//             ClientNode node1 = new ClientNode(InetAddress.getLocalHost(), 21, Generator.randomString(10));
        
//             AFile file1 = new AFile("file1","file1", node1);
//             AFile file3 = new AFile("file3","file3",node1);

        
//             filesOnNode.add(file1);
//             filesOnNode.add(file3);

//             apiUtilMockedStatic.when(() -> ApiUtil.clientGetAllFiles(anyString(), eq(ClientNodeConfig.API_PORT)))
//                     .thenReturn("[{\"name\":\"file1\", \"isReplicated\":false}, {\"name\":\"file3\", \"isReplicated\":false}]");

//             try (MockedStatic<FileControl> dataMockedStatic = mockStatic(FileControl.class)) {
//                 dataMockedStatic.when(FileControl::getFirstLockRequest).thenReturn("file1");
//                 dataMockedStatic.when(() -> FileControl.addAcceptedLock(anyString())).thenReturn(true);
    
//                 syncAgent.run();
    
//                 assertTrue(syncAgent.getFiles().get("file1"));
//             }

//         }
//         catch(Exception ex){
//             assertFalse(true);
//         }
//     }

//     @Test
//     public void testProcessUnlockRequest() {

//         try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class);
//              MockedStatic<ApiUtil> apiUtilMockedStatic = mockStatic(ApiUtil.class);
//              MockedStatic<JsonConverter> jsonConverterMockedStatic = mockStatic(JsonConverter.class);
//              MockedStatic<FileControl> dataMockedStatic = mockStatic(FileControl.class)) {

//             InetAddress mockInetAddress = mock(InetAddress.class);
//             inetAddressMockedStatic.when(InetAddress::getLocalHost).thenReturn(mockInetAddress);
//             when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
            
//             List<AFile> filesOnNode = new ArrayList<>();
//             ClientNode node1 = new ClientNode(InetAddress.getLocalHost(), 21, Generator.randomString(10));
        
//             AFile file1 = new AFile("file1","file1", node1);
//             AFile file3 = new AFile("file3","file3",node1);

        
//             filesOnNode.add(file1);
//             filesOnNode.add(file3);

//             apiUtilMockedStatic.when(() -> ApiUtil.clientGetAllFiles(anyString(), eq(ClientNodeConfig.API_PORT)))
//                     .thenReturn("[{\"name\":\"file1\", \"isReplicated\":false}, {\"name\":\"file3\", \"isReplicated\":false}]");

//             dataMockedStatic.when(FileControl::getFirstUnlockRequest).thenReturn("file2");
        
//             syncAgent.run();
            
//             assertFalse(syncAgent.getFiles().get("file2"));
//         }
//         catch(Exception ex){
//             assertFalse(true);
//         }
//     }
    
// }
