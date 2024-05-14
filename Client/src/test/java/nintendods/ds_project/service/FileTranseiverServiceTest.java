package nintendods.ds_project.service;

import nintendods.ds_project.exeption.DuplicateFileException;
import nintendods.ds_project.model.ANetworkNode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.file.IFileConditionChecker;
import nintendods.ds_project.utility.FileModifier;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.assertTrue;

@SpringBootTest
public class FileTranseiverServiceTest {

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

            //Delete so we can simulate a new node.
            testFile.delete();

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            // Wait for an incomming message.
            while (!ok) {
                newFileObject = ftss.saveIncomingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            assertTrue(ok);

            // Delete the used files in the test
            testFile.delete();
            new File(newFileObject.getAbsolutePath()).delete();
        } catch (IOException | DuplicateFileException e) {
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

            File newFile = FileModifier.createFile(testFile.getName(), true);

            assertTrue(testFile.exists() && newFile.exists());
            System.out.println("Existing file name: " + testFile.getName());
            System.out.println("new file name: " + newFile.getName());
            testFile.delete();
            newFile.delete();
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

            //Delete so we can simulate a new node.
            testFile.delete();

            //create new node
            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            // Create custom folder in the same project directory

            String newPath = System.getProperty("user.dir") +  "/newDir";

            // Wait for an incomming message.
            while (!ok) {
                newFileObject = ftss.saveIncomingFile(nodeRec, newPath);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            // show logs of file
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files in the test and the directory
            new File(newFileObject.getAbsolutePath()).delete();
            new File(newPath).delete();
        } catch (IOException | DuplicateFileException e) {
            assertTrue(false);
        }
    }


    @Test
    public void testFileTransferWithConditionSave() {
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

            //Delete so we can simulate a new node.
            testFile.delete();

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            // Wait for an incomming message.
            while (!ok) {
                newFileObject = ftss.saveFileWithConditions(nodeRec, new TestCondition(), true);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            // show logs of file
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files in the test and the directory
            
            new File(newFileObject.getAbsolutePath()).delete();
            //new File(newFileObject.getAbsolutePath()).delete();
            //new File(newPath).delete();
        } catch (IOException | DuplicateFileException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testFileTransferWithConditionSaveCustomFolder() {
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

            //Delete so we can simulate a new node.
            testFile.delete();

            ANetworkNode nodeRec = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe receive");

            boolean ok = false;
            AFile newFileObject = null;

            // Create custom folder in the same project directory

            String newPath = System.getProperty("user.dir") +  "\\newDir";

            // Wait for an incomming message.
            while (!ok) {
                newFileObject = ftss.saveFileWithConditions(nodeRec, newPath, new TestCondition(), true);
                if (newFileObject != null) {
                    ok = true;
                }
            }

            // Check if file exists on the system
            assertTrue(new File(newFileObject.getAbsolutePath()).exists());

            // show logs of file
            System.out.println(newFileObject.getFormattedLogs());

            // Delete the used files in the test and the directory
            new File(newFileObject.getAbsolutePath()).delete();
            new File(newFileObject.getAbsolutePath()).delete();
            FileUtils.cleanDirectory(new File(newFileObject.getDirPath())); 
            new File(newPath).delete();
        } catch (IOException | DuplicateFileException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testFileTransferThrowDuplicateFile() {
        File testFile = null;
        try {
            // A file on the system is created
            testFile = new File("TestFile2.txt");
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

            // Wait for an incomming message.
            while (!ok) {
                newFileObject = ftss.saveIncomingFile(nodeRec);
                if (newFileObject != null) {
                    ok = true;
                }
            }

        } catch (IOException | DuplicateFileException e) {

            //We should enter the exception
            if (e instanceof DuplicateFileException){
                assertTrue(true);
                //Delete so we can simulate a new node.
                if(testFile != null)
                    testFile.delete();
            }
            else
                assertTrue(false);
        }
    }
}

// used in the test as save file condition
final class TestCondition implements IFileConditionChecker{
    @Override
    public boolean getFileCondition(AFile file) {
        if (file.getName().equals("TestFile2.txt")){
            return true;
        }
        return false;
    }

}