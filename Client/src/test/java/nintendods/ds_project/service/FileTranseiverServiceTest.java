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
/*
    @Test
    public void testFileTransfer() {
        try {
            System.out.println(System.getProperty("user.dir"));

            // A file on the system is created
            File testFile = new File("TestFile0.txt");
            if (testFile.createNewFile()) {
                FileWriter fw = new FileWriter(testFile);
                fw.append("This is a text in the file!");
                fw.close();
            }

            // A node is created
            ANetworkNode nodeSend = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe");

            // Node sees if a file is on his system and it will create an object of it.
            AFile fileObj = new AFile(testFile.getAbsolutePath(), testFile.getName(), nodeSend);

            // Create FileTranseiver object and transfer file
            FileTransceiverService ftss = new FileTransceiverService(12344, 20);

            // Send the file to itself. Can be any node received from the namingserver.
            ftss.sendFile(fileObj, nodeSend.getAddress().getHostAddress());

            // receiver side must create FileTranseiverService to receive incomming
            // messages.
            // Here a node has to be created ofcource and the create FileTranseiver object.

            // I can not create the FileTranseiverService because it uses a specific TCP
            // port to
            // listen on. And on this test pc the port is already in use. therefore I use
            // the receive
            // thread from the aleady created FileTranseiverService object.

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            // Wait for an incomming message.
            while (!ok) {
                newFileObject = ftss.saveIncommingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            assertTrue(ok);

            // Delete the used files in the test
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testFileTransferDuplicate() {
        try {
            // A file on the system is created
            File testFile = new File("TestFile1.txt");
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
            FileTransceiverService ftss = new FileTransceiverService(12346, 20);

            // Send the file to itself. Can be any node received from the namingserver.
            ftss.sendFile(fileObj, nodeSend.getAddress().getHostAddress());

            // receiver side must create FileTranseiverService to receive incomming
            // messages.
            // Here a node has to be created ofcource and the create FileTranseiver object.

            // I can not create the FileTranseiverService because it uses a specific TCP
            // port to
            // listen on. And on this test pc the port is already in use. therefore I use
            // the receive
            // thread from the aleady created FileTranseiverService object.
            // FileTranseiverService ftssRes = new FileTranseiverService();

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            while (!ok) {
                newFileObject = ftss.saveIncommingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            System.out.println(newFileObject.getFormattedLogs());
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testFileTransferWithCustomFolder() {
        try {
            // A file on the system is created
            File testFile = new File("TestFile2.txt");
            if (testFile.createNewFile()) {
                FileWriter fw = new FileWriter(testFile);
                fw.append("This is a text in the file!");
                fw.close();
            }

            ANetworkNode nodeSend = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe");
            AFile fileObj = new AFile(testFile.getAbsolutePath(), testFile.getName(), nodeSend);

            FileTransceiverService ftss = new FileTransceiverService(12347, 20);

            // Send the file to itself. Can be any node received from the namingserver.
            ftss.sendFile(fileObj, nodeSend.getAddress().getHostAddress());

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            // Create custom folder in the same project directory

            String newPath = System.getProperty("user.dir") +  "/newDir";

            // Wait for an incomming message.
            while (!ok) {
                // newFileObject = ftss.saveIncommingFile(nodeRec, "/home/robbe/Documents");
                newFileObject = ftss.saveIncommingFile(nodeRec, newPath);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            // show logs of file
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files in the test and the directory
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();
            new File(newPath).delete();
        } catch (IOException e) {
            assertTrue(false);
        }
    }*/
}