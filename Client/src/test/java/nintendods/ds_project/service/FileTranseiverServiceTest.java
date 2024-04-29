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

            // receiver side must create FileTranseiverService to receive incomming messages.
            // Here a node has to be created ofcource and the create FileTranseiver object.

            // I can not create the FileTranseiverService because it uses a specific TCP port to 
            // listen on. And on this test pc the port is already in use. therefore I use the receive 
            // thread from the aleady created FileTranseiverService object.
            // FileTranseiverService ftssRes = new FileTranseiverService();

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            //Wait for an incomming message.
            while (!ok) {
                // newFileObject = ftss.saveIncommingFile(nodeRec, "/home/robbe/Documents");
                newFileObject = ftss.saveIncommingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            //TEST checks
            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());
            // show logs
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files in the test
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // gets the .tmp suffix

        // Create file object
    }

    @Test
    public void testFileTransferWithCustomFolder() {
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

            // receiver side must create FileTranseiverService to receive incomming messages.
            // Here a node has to be created ofcource and the create FileTranseiver object.

            // I can not create the FileTranseiverService because it uses a specific TCP port to 
            // listen on. And on this test pc the port is already in use. therefore I use the receive 
            // thread from the aleady created FileTranseiverService object.
            // FileTranseiverService ftssRes = new FileTranseiverService();

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            //Create custom folder in the same project directory
            File temp = new File("temp");

            if (temp.createNewFile()) {
                FileWriter fw = new FileWriter(temp);
                fw.close();
            }

            String newPath = temp.getAbsolutePath();
            newPath = newPath.replace("temp", "newDir");

            System.out.println(newPath);

            //Wait for an incomming message.
            while (!ok) {
                // newFileObject = ftss.saveIncommingFile(nodeRec, "/home/robbe/Documents");
                newFileObject = ftss.saveIncommingFile(nodeRec, newPath);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            //TEST checks
            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());
            // show logs
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files in the test
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // gets the .tmp suffix

        // Create file object
    }
}