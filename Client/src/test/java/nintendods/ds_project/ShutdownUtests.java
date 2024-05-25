package nintendods.ds_project;

import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.service.FileDBService;
import nintendods.ds_project.service.FileTransceiverService;
import nintendods.ds_project.utility.ApiUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(args = {"--TESTING=1"})
public class ShutdownUtests {

    @Autowired
    private Client client;

    /**
     * test if all files will be send over from the database
     *
     * @throws Exception
     */
    @Test
    void replicationShutdownTest_EmptyDataBase() throws Exception { //if the test fails check if "test/repliaction/" or any sub dirs have txt files. If yes -> delete them

        UNAMObject unamObject = new UNAMObject(1, eMessageTypes.UnicastNamingServerToNode, 1, "127.0.0.1", 10);
        FileTransceiverService fileTransceiverService = new FileTransceiverService(12347, 1000);
        client.setFileTransceiver(fileTransceiverService);
        client.setNsObject(unamObject);

        FileDB fileDB = FileDBService.getFileDB();

        List<AFile> original_files = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String fileName = "testFile" + i + ".txt";
            String absolutePath = System.getProperty("user.dir") + "/test/replication/" + fileName;

            File testFile = new File(absolutePath);
            if (testFile.createNewFile()) {
                FileWriter fw = new FileWriter(testFile);
                fw.append("This is a text in the file named: ").append(fileName);
                fw.close();
            }


            AFile file = new AFile(testFile.getAbsolutePath(), testFile.getName(), client.node);
            original_files.add(file);
            fileDB.addOrUpdateFile(file);
        }


        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        when(mockRestTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok("127.0.0.1"));

        ApiUtil.setRestTemplate(mockRestTemplate);

        client.prepareForShutdown();


        // Wait for an incomming message.
        fileTransceiverService.testing_justReadFiles = true;
        int fileCounter = 0;
        List<AFile> newfiles = new ArrayList<>();
        while (true) {
            AFile newFileObject = fileTransceiverService.saveIncomingFile(client.node, (System.getProperty("user.dir") + "/test/replication/return/"));
            if (newFileObject == null) {
                break;
            } else {
                assertTrue(areFilesSame(newFileObject, original_files.get(fileCounter)));
                fileCounter++;
                newfiles.add(newFileObject);
            }
        }

        newfiles.forEach(afile -> afile.getFile().delete()); // does not work on all PC -> "The process cannot access the file because it is being used by another process"
        original_files.forEach(afile -> afile.getFile().delete());

    }

    @Test
    void replicationShutdownTest_RecieveOriginalFile() throws Exception { //if the test fails check if "test/repliaction/" or any sub dirs have txt files. If yes -> delete them

        UNAMObject unamObject = new UNAMObject(1, eMessageTypes.UnicastNamingServerToNode, 1, "127.0.0.1", 10);
        FileTransceiverService fileTransceiverService = new FileTransceiverService(12347, 1000);
        client.setFileTransceiver(fileTransceiverService);
        client.setNsObject(unamObject);


        String fileName = "testFile_OG.txt";
        String absolutePath = System.getProperty("user.dir") + "/test/replication/" + fileName;
        File testFile = new File(absolutePath);
        if (testFile.createNewFile()) {
            FileWriter fw = new FileWriter(testFile);
            fw.append("This is a text in the file named: ").append(fileName);
            fw.close();
        }
        AFile file = new AFile(testFile.getAbsolutePath(), testFile.getName(), client.node);

        file.setReplicated(true, "10.10.10.10");

        fileTransceiverService.sendFile(file, "127.0.0.1");

        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            String url = invocation.getArgument(0, String.class);
            Class<?> responseType = invocation.getArgument(1, Class.class);
            System.out.println("getForEntity called with URL: " + url + ", ResponseType: " + responseType.getSimpleName());
            return ResponseEntity.ok("127.0.0.1");
        }).when(mockRestTemplate).getForEntity(anyString(), eq(String.class));

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            String url = invocation.getArgument(0, String.class);
            HttpMethod method = invocation.getArgument(1, HttpMethod.class);
            HttpEntity<?> requestEntity = invocation.getArgument(2, HttpEntity.class);
            Class<?> responseType = invocation.getArgument(3, Class.class);
            System.out.println("exchange called with URL: " + url + ", Method: " + method + ", RequestEntity: " + requestEntity + ", ResponseType: " + responseType.getSimpleName());
            return ResponseEntity.ok("127.0.0.1");
        }).when(mockRestTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));


        ApiUtil.setRestTemplate(mockRestTemplate);


        AFile newFileObject = fileTransceiverService.saveIncomingFile(client.node, (System.getProperty("user.dir") + "/test/replication/return/"));

        assertTrue(areFilesSame(newFileObject, file));
        assertTrue(newFileObject.isBeenBackedUp());

        deletAfile(file);
        deletAfile(newFileObject);
    }

    @Test
    void replicationShutdownTest_RecieveBackUpFile_OriginalNotInDB() throws Exception { //if the test fails check if "test/repliaction/" or any sub dirs have txt files. If yes -> delete them

        UNAMObject unamObject = new UNAMObject(1, eMessageTypes.UnicastNamingServerToNode, 1, "127.0.0.1", 10);
        FileTransceiverService fileTransceiverService = new FileTransceiverService(12347, 1000);
        client.setFileTransceiver(fileTransceiverService);
        client.setNsObject(unamObject);


        String fileName = "testFile_BackUp_NoOGinDB.txt";
        String absolutePath = System.getProperty("user.dir") + "/test/replication/" + fileName;
        File testFile = new File(absolutePath);
        if (testFile.createNewFile()) {
            FileWriter fw = new FileWriter(testFile);
            fw.append("This is a text in the file named: ").append(fileName);
            fw.close();
        }
        AFile file = new AFile(testFile.getAbsolutePath(), testFile.getName(), client.node);

        fileTransceiverService.sendFile(file, "127.0.0.1");


        TimeUnit.SECONDS.sleep(1);


        AFile newFileObject = fileTransceiverService.saveIncomingFile(client.node, (System.getProperty("user.dir") + "/test/replication/return/"));
        TimeUnit.SECONDS.sleep(1);
        assertTrue(areFilesSame(newFileObject, file));
        assertTrue(FileDBService.getFileDB().getFiles().stream().anyMatch(file1 -> {
            try {
                return areFilesSame(file1, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        deletAfile(file);
        deletAfile(newFileObject);
    }

    @Test
    void replicationShutdownTest_RecieveBackUpFile_OriginalInDB() throws Exception { //if the test fails check if "test/repliaction/" or any sub dirs have txt files. If yes -> delete them

        UNAMObject unamObject = new UNAMObject(1, eMessageTypes.UnicastNamingServerToNode, 1, "127.0.0.1", 10);
        FileTransceiverService fileTransceiverService = new FileTransceiverService(12347, 1000);
        client.setFileTransceiver(fileTransceiverService);
        client.setNsObject(unamObject);


        String fileName = "testFile_BackUp_OGinDB.txt";
        String absolutePath = System.getProperty("user.dir") + "/test/replication/" + fileName;
        File testFile = new File(absolutePath);
        if (testFile.createNewFile()) {
            FileWriter fw = new FileWriter(testFile);
            fw.append("This is a text in the file named: ").append(fileName);
            fw.close();
        }

        AFile fileInDB = new AFile(testFile.getAbsolutePath(), testFile.getName(), client.node);
        AFile fileBackup = new AFile(testFile.getAbsolutePath(), testFile.getName(), client.node);

        fileInDB.setReplicated(true, "10.10.10.10");
        FileDBService.getFileDB().addOrUpdateFile(fileInDB);

        fileTransceiverService.sendFile(fileBackup, "127.0.0.1");

        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        doAnswer((Answer<ResponseEntity<String>>) invocation -> {
            String url = invocation.getArgument(0, String.class);
            Class<?> responseType = invocation.getArgument(1, Class.class);
            System.out.println("getForEntity called with URL: " + url + ", ResponseType: " + responseType.getSimpleName());
            return ResponseEntity.ok("127.0.0.1");
        }).when(mockRestTemplate).getForEntity(anyString(), eq(String.class));

        ApiUtil.setRestTemplate(mockRestTemplate);

        assertNull(fileTransceiverService.saveIncomingFile(client.node, (System.getProperty("user.dir") + "/test/replication/return/")));

        TimeUnit.SECONDS.sleep(1);

        //deleting file in DB to simulating being in a new node
        FileDBService.getFileDB().removeFile(fileInDB.getName());

        AFile newFile = fileTransceiverService.saveIncomingFile(client.node, (System.getProperty("user.dir") + "/test/replication/return/"));

        assertTrue(areFilesSame(newFile, fileBackup));
        assertFalse(newFile.isBeenBackedUp());
    }

    private void deletAfile(AFile file) {
        if (!file.getFile().delete()) {
            System.out.println("sorry, couldn't delete: " + file.getName());
        }

    }

    private boolean areFilesSame(AFile file1, AFile file2) throws IOException {
        return file1.getName().equals(file2.getName()) && compareFileContents(file1.getFile(), file2.getFile());
    }

    private boolean compareFileContents(File file1, File file2) throws IOException {

        byte[] f1 = Files.readAllBytes(Path.of(file1.getAbsolutePath()));
        byte[] f2 = Files.readAllBytes(Path.of(file2.getAbsolutePath()));
        return java.util.Arrays.equals(f1, f2);
    }
}
