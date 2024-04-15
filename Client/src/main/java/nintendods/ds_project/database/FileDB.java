package nintendods.ds_project.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository class for storing and managing file information in the distributed system.
 * It handles operations like adding, updating, removing, and querying file locations.
 */
@Repository
public class FileDB {
    private static final Logger logger = LoggerFactory.getLogger(FileDB.class);
    private final ConcurrentHashMap<String, String> fileNameToNodeIP; // Maps file names to node IPs for quick retrieval.

    public FileDB() {
        fileNameToNodeIP = new ConcurrentHashMap<>();
    }

    /**
     * Adds or updates a file in the database.
     * If the file already exists, its node IP will be updated.
     * @param fileName The name of the file.
     * @param nodeIP The IP address of the node storing the file.
     */
    public void addOrUpdateFile(String fileName, String nodeIP) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for addOrUpdateFile.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        if (nodeIP == null || nodeIP.trim().isEmpty()) {
            logger.error("Invalid node IP provided for addOrUpdateFile.");
            throw new IllegalArgumentException("Node IP cannot be null or empty.");
        }
        fileNameToNodeIP.put(fileName, nodeIP);
        logger.info("File '{}' updated/added with new node IP '{}'.", fileName, nodeIP);
    }

    /**
     * Retrieves the IP address of the node storing the specified file.
     * @param fileName The name of the file.
     * @return The IP address of the node or null if the file is not found.
     */
    public String getFileLocation(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for getFileLocation.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        return fileNameToNodeIP.get(fileName);
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
        boolean removed = fileNameToNodeIP.remove(fileName) != null;
        if (removed) {
            logger.info("File '{}' was removed from the database.", fileName);
        } else {
            logger.warn("Attempted to remove '{}' but it did not exist in the database.", fileName);
        }
        return removed;
    }

    /**
     * Checks if a file exists in the database.
     * @param fileName The name of the file.
     * @return true if the file exists, false otherwise.
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for fileExists.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        return fileNameToNodeIP.containsKey(fileName);
    }
}
