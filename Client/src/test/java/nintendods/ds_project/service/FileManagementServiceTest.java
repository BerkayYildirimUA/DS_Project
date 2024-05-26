package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = FileManagementService.class)
public class FileManagementServiceTest {

    //@Autowired
    //private FileManagementService fileManagementService;

    @MockBean
    private FileDB fileDB;

    @Mock
    private NSAPIService nsapiService;
/*
    @Test
    public void testCalculateFileHashes() {
        // Setup
        fileManagementService.getFileDB().addOrUpdateFile("file1.txt", eFileTypes.Local, "192.168.1.100");
        fileManagementService.getFileDB().addOrUpdateFile("file2.txt", eFileTypes.Local, "192.168.1.101");
        // Exercise
        Map<String, Integer> hashes = fileManagementService.calculateFileHashes();

        // Verify
        assertNotNull(hashes);
        assertEquals(2, hashes.size());  // Assert we have hashes for both files
        assertTrue(hashes.containsKey("file1.txt"));
        assertTrue(hashes.containsKey("file2.txt"));

        // Validate hash values (Optional: depends on predictable output from NameToHash)
        assertEquals(NameToHash.convert("file1.txt"), hashes.get("file1.txt"));
        assertEquals(NameToHash.convert("file2.txt"), hashes.get("file2.txt"));
    }*/
}
