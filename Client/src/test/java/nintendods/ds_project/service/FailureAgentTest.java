package nintendods.ds_project.service;

import nintendods.ds_project.Client;
import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.utility.NameToHash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(args = {"--TESTING=1"})
public class FailureAgentTest {

    @Autowired
    private Client client;

    @Test // Unfinished test: used for manual checking (with breakpoints and such), but automated tests are yet to be implemented (ran out of time)
    public void failureAgent_UTest() throws IOException {

        UNAMObject unamObject = new UNAMObject(1, eMessageTypes.UnicastNamingServerToNode, 1, "127.0.0.1", 10);
        FileTransceiverService fileTransceiverService = new FileTransceiverService(200, 1000);
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
        AFile file = new AFile(testFile.getAbsolutePath(), testFile.getName(), client.getNode());

        file.setDownloadLocation(  String.valueOf(NameToHash.convert("testFile_OG")));

        FileDBService.getFileDB().addOrUpdateFile(file);

        //new FailureAgent(String.valueOf(NameToHash.convert("testFile_OG")), String.valueOf(client.getNode().getId()), fileTransceiverService).run();

    }













}
