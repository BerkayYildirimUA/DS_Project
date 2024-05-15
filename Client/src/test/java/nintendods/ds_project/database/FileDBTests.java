package nintendods.ds_project.database;
import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.utility.FileReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "multicast.address=224.0.0.100",
        "multicast.port=12345",
        "multicast.buffer-capacity=20"
})
class FileDBTests {
    private final String path = System.getProperty("user.dir") + "/assets";

    @Test
    void testAddOrUpdateFile() {
        FileDB fileDB = new FileDB();
        List<File> files = FileReader.getFiles(path);
        fileDB.addOrUpdateFile(files.getFirst(), new ANode("node"));

        Optional<AFile> dbFile = fileDB.getFile(files.getFirst().getName());
        if (dbFile.isPresent())   assertEquals(files.getFirst().getName(), dbFile.get().getName());
        else assertFalse(true);
    }

    @Test
    void testRemoveFile() {
        FileDB fileDB = new FileDB();
        List<File> files = FileReader.getFiles(path);
        fileDB.addOrUpdateFile(files.getFirst(), new ANode("node"));

        assertTrue(fileDB.removeFile(files.getFirst().getName()));
        assertFalse(fileDB.fileExists(files.getFirst().getName()));
    }

    @Test
    void testFileExists() {
        FileDB fileDB = new FileDB();
        List<File> files = FileReader.getFiles(path);
        fileDB.addOrUpdateFile(files.getFirst(), new ANode("node"));

        assertTrue(fileDB.fileExists(files.getFirst().getName()));
        assertFalse(fileDB.fileExists("nonExistentFile.txt"));
    }
}