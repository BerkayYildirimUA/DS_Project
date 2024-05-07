package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertSame;
@SpringBootTest(properties = {
        "multicast.address=224.0.0.100",
        "multicast.port=12345",
        "multicast.buffer-capacity=20"
})
class FileDBServiceTests {

    @Test
    void testSingletonFileDBInstance() {
        FileDB firstInstance = FileDBService.getFileDB();
        FileDB secondInstance = FileDBService.getFileDB();
        assertSame(firstInstance, secondInstance, "Both instances should be the same");
    }
}