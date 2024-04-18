package nintendods.ds_project.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class NodeModelTest {

    @Test
    void createFileTest() {
        // Arrange
        NodeModel node = new NodeModel(1, null, 8080);

        // Act
        node.createFile("file1.txt");

        // Assert
        assertTrue(node.getLocalFiles().contains("file1.txt"));
    }

    @Test
    void deleteFileTest() {
        // Arrange
        NodeModel node = new NodeModel(1, null, 8080);
        node.createFile("file1.txt");

        // Act
        node.deleteFile("file1.txt");

        // Assert
        assertFalse(node.getLocalFiles().contains("file1.txt"));
    }

    @Test
    void convertFileNameToHashTest() {
        // Arrange
        NodeModel node = new NodeModel(1, null, 8080);

        // Act
        int hashValue = node.convertFileNameToHash("file1.txt");

        // Assert
        assertNotNull(hashValue);
    }

    @Test
    void getFileTest_localFileExists() {
        // Arrange
        NodeModel node = new NodeModel(1, null, 8080);
        node.createFile("file1.txt");

        // Act
        String fileContent = node.getFile("file1.txt");

        // Assert
        assertEquals("Content of the file: file1.txt", fileContent);
    }

    @Test
    void getFileTest_remoteFileExists() {
        // Arrange
        NodeModel node = new NodeModel(1, null, 8080);
        node.getRemoteFiles().add("file1.txt");

        // Act
        String fileContent = node.getFile("file1.txt");

        // Assert
        assertEquals("Content of the file: file1.txt", fileContent);
    }

    @Test
    void getFileTest_fileNotExists() {
        // Arrange
        NodeModel node = new NodeModel(1, null, 8080);

        // Act
        String fileContent = node.getFile("non_existing_file.txt");

        // Assert
        assertEquals("File not found", fileContent);
    }
}
