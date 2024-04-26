package nintendods.ds_project.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import nintendods.ds_project.model.ANetworkNode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.FileMessage;

@SpringBootTest
public class FileTranseiverServiceTest {
    @Test
    public void testFileTransfer() {
        try {
            File testFile = new File("TestFile.txt");
            if (testFile.createNewFile()) {
                FileWriter fw = new FileWriter(testFile);
                fw.append("This is a text in the file!");
                fw.close();
            }

            ANetworkNode nodeSend = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe");
            // Setup AFile object
            AFile fileObj = new AFile(testFile.getAbsolutePath(), testFile.getName(), nodeSend);

            fileObj.getDirPath();

            // Create FileTranseiver object and transfer file
            FileTranseiverService ftss = new FileTranseiverService();

            ftss.sendFile(fileObj, nodeSend.getAddress().getHostAddress());

            System.out.println("Sended over");

            try {
                Thread.sleep(500); // Sleep for 500 milliseconds
            } catch (InterruptedException e) {
                // Handle interruption if needed
                e.printStackTrace();
            }

            //receiver side
            // Create FileTranseiver object and transfer file
            //FileTranseiverService ftssRes = new FileTranseiverService();
            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            while (!ok) {
                //newFileObject = ftss.saveIncommingFile(nodeRec, "/home/robbe/Documents");
                newFileObject = ftss.saveIncommingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            //show logs
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // gets the .tmp suffix

        // Create file object
    }
}
