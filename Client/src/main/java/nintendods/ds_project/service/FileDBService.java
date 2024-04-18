package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import org.springframework.stereotype.Service;

/**
 * Singleton service for accessing the file database.
 */
@Service
public class FileDBService {
    private static FileDB fileDB;

    /**
     * Returns the singleton instance of the FileDB.
     * @return The singleton instance of the FileDB.
     */
    public static FileDB getFileDB() {
        if (fileDB == null) {
            fileDB = new FileDB();
        }
        return fileDB;
    }
}