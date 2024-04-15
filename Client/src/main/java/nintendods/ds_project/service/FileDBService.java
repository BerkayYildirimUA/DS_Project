/**
 * Singleton service for accessing the file database.
 */
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