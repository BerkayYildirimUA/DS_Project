package nintendods.ds_project.database;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = {
        "multicast.address=224.0.0.100",
        "multicast.port=12345",
        "multicast.buffer-capacity=20"
})
class FileDBTests {
    private final String path = System.getProperty("user.dir") + "/assets";

    // @Test
    // void testAddOrUpdateFile() {
    //     FileDB fileDB = new FileDB();
    //     fileDB.addOrUpdateFile("testFile.txt", "10.10.10.10");
    //     assertEquals("10.10.10.10", fileDB.getFileLocation("testFile.txt"));
    // }

    // @Test
    // void testRemoveFile() {
    //     FileDB fileDB = new FileDB();
    //     fileDB.addOrUpdateFile("testFile.txt", "10.10.10.10");
    //     assertTrue(fileDB.removeFile("testFile.txt"));
    //     assertFalse(fileDB.fileExists("testFile.txt"));
    // }

    // @Test
    // void testFileExists() {
    //     FileDB fileDB = new FileDB();
    //     fileDB.addOrUpdateFile("testFile.txt", "10.10.10.10");
    //     assertTrue(fileDB.fileExists("testFile.txt"));
    //     assertFalse(fileDB.fileExists("nonExistentFile.txt"));
    // }
}