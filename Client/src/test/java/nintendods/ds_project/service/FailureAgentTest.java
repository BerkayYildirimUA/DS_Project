package nintendods.ds_project.service;

import nintendods.ds_project.Client;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.model.message.eMessageTypes;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@SpringBootTest(args = {"--TESTING=1"})
public class FailureAgentTest {

    @Autowired
    private Client client;

    @Test
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

        file.setReplicated(true, "10.10.10.10");

        new FailureAgent(String.valueOf(10), String.valueOf(client.getNode().getId()), fileTransceiverService).run();

    }













}
