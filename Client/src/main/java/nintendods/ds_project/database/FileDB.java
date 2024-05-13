package nintendods.ds_project.database;
import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.AFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository class for storing and managing file information in the distributed system.
 * It handles operations like adding, updating, removing, and querying file locations.
 */
@Repository
public class FileDB {
    private static final Logger logger = LoggerFactory.getLogger(FileDB.class);
    private final List<AFile> fileDB = new ArrayList<>();

    public FileDB() { }

    /**
     * Adds or updates a file in the database. If the file already exists, its node IP will be updated.
     * This method throws IllegalArgumentException if the provided fileName or nodeIP is null or empty.
     *
     * @param file The name of the file. Must be non-null and not empty.
     * @param node The IP address of the node storing the file. Must be non-null and not empty.
     * @throws IllegalArgumentException If fileName or nodeIP is null or empty.
     */
    public void addOrUpdateFile(File file, ANode node) {
        if (file == null) {
            logger.error("Invalid file provided for addOrUpdateFile.");
            throw new IllegalArgumentException("File cannot be null.");
        }
        AFile aFile = new AFile(file.getAbsolutePath(), file.getName(), node);
        addOrUpdateFile(aFile);
    }

    public void addOrUpdateFile(AFile file) {
        if (file == null) {
            logger.error("Invalid file provided for addOrUpdateFile.");
            throw new IllegalArgumentException("File cannot be null.");
        }

        if (fileExists(file.getName())) {
            // File exist and needs to be changed in DB
            Optional<AFile> oldFile = getFile(file.getName());
            if (oldFile.isPresent()) {
                int index = fileDB.indexOf(oldFile.get());
                fileDB.set(index, file);
                logger.info("File '{}' updated.", file.getName());
            }
        } else {
            // File doesn't exist and needs to be added to the DB
            fileDB.add(file);
            logger.info("File '{}' added.", file.getName());
        }
    }

    /**
     * Retrieves the IP address of the node storing the specified file.
     * @param fileName The name of the file.
     * @return The IP address of the node or null if the file is not found.
     */
    public Optional<AFile> getFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            logger.error("Invalid file name provided for getFile.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        return fileDB.stream().filter(file -> file.getName().equals(fileName)).findFirst();
    }


    public List<AFile> getFiles() {
        return this.fileDB;
    }

    /**
     * Removes a file from the database if it exists.
     * @param fileName The name of the file to remove.
     * @return true if the file was removed, false if it did not exist.
     */
    public boolean removeFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for removeFile.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        boolean removed = false;
        Optional<AFile> oldFile = getFile(fileName);
        if (oldFile.isPresent()) {
            int index = fileDB.indexOf(oldFile.get());
            removed = fileDB.remove(index) != null;
        }

        if (removed)    logger.info("File '{}' was removed from the database.", fileName);
        else            logger.warn("Attempted to remove '{}' but it did not exist in the database.", fileName);

        return removed;
    }

    /**
     * Checks if a file exists in the database.
     * @param fileName The name of the file.
     * @return true if the file exists, false otherwise.
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            logger.error("Invalid file name provided for fileExists.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        return fileDB.stream().anyMatch(file -> file.getName().equals(fileName));
    }
}