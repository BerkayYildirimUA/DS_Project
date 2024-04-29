package nintendods.ds_project.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import nintendods.ds_project.model.ANetworkNode;
import nintendods.ds_project.model.file.AFile;

@SpringBootTest
public class FileTranseiverServiceTest {
    @Test
    public void testFileTransfer() {
        try {

            // A file on the system is created
            File testFile = new File("TestFile.txt");
            if (testFile.createNewFile()) {
                FileWriter fw = new FileWriter(testFile);
                fw.append("This is a text in the file!");
                fw.close();
            }

            // A node is created
            ANetworkNode nodeSend = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe");

            // Node sees if a file is on his system and it will create an object of it.
            AFile fileObj = new AFile(testFile.getAbsolutePath(), testFile.getName(), nodeSend);

            // Lets say that the file needs to be transfered
            // Create FileTranseiver object and transfer file
            FileTranseiverService ftss = new FileTranseiverService();

            // Send the file to itself. Can be any node received from the namingserver.
            ftss.sendFile(fileObj, nodeSend.getAddress().getHostAddress());
            System.out.println("Sended over");

            try {
                Thread.sleep(500); // Sleep for 500 milliseconds
            } catch (InterruptedException e) {
                // Handle interruption if needed
                e.printStackTrace();
            }

            // receiver side
            // Create FileTranseiver object and transfer file
            // FileTranseiverService ftssRes = new FileTranseiverService();
            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            while (!ok) {
                // newFileObject = ftss.saveIncommingFile(nodeRec, "/home/robbe/Documents");
                newFileObject = ftss.saveIncommingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            // show logs
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
