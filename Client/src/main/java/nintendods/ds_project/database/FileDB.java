package nintendods.ds_project.database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository class for storing and managing file information in the distributed system.
 * It handles operations like adding, updating, removing, and querying file locations.
 */
@Repository
public class FileDB {
    private static final Logger logger = LoggerFactory.getLogger(FileDB.class);
    // Maps a compound key of file name and file type to node IPs for quick retrieval
    private final ConcurrentHashMap<Map.Entry<String, eFileTypes>, String> fileNameToNodeIP;

    public FileDB() {
        this.fileNameToNodeIP = new ConcurrentHashMap<>();
    }

    /**
     * Adds or updates a file in the database. If the file already exists, its node IP will be updated.
     * This method throws IllegalArgumentException if the provided fileName, fileType, or nodeIP is null or empty.
     *
     * @param fileName The name of the file. Must be non-null and not empty.
     * @param fileType The type of the file (Local or Replicated).
     * @param nodeIP The IP address of the node storing the file. Must be non-null and not empty.
     * @throws IllegalArgumentException If any argument is null or empty.
     */
    public void addOrUpdateFile(String fileName, eFileTypes fileType, String nodeIP) {
        if (fileName == null || fileName.trim().isEmpty() || fileType == null) {
            logger.error("Invalid file name or type provided for addOrUpdateFile.");
            throw new IllegalArgumentException("File name and type cannot be null or empty.");
        }
        if (nodeIP == null || nodeIP.trim().isEmpty()) {
            logger.error("Invalid node IP provided for addOrUpdateFile.");
            throw new IllegalArgumentException("Node IP cannot be null or empty.");
        }
        this.fileNameToNodeIP.put(Map.entry(fileName, fileType), nodeIP);
        logger.info("File '{}' of type '{}' updated/added with new node IP '{}'.", fileName, fileType, nodeIP);
    }

    /**
     * Retrieves the IP address of the node storing the specified file, determining the file type internally.
     * @param fileName The name of the file.
     * @return The IP address of the node or null if the file is not found, along with file type.
     */
    public String getFileLocation(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for getFileLocation.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        eFileTypes fileType = determineFileType(fileName); // Hypothetical method to determine file type
        if (fileType == null) {
            logger.error("File type could not be determined for '{}'.", fileName);
            return null;
        }
        return fileNameToNodeIP.get(Map.entry(fileName, fileType));
    }

    /**
     * Determines if a file is local or replicated based on the keys in the ConcurrentHashMap.
     * @param fileName The name of the file to check.
     * @return eFileTypes indicating if the file is local or replicated, or null if it cannot be determined.
     */
    private eFileTypes determineFileType(String fileName) {
        for (Map.Entry<Map.Entry<String, eFileTypes>, String> entry : fileNameToNodeIP.entrySet()) {
            if (entry.getKey().getKey().equals(fileName)) {
                return entry.getKey().getValue();  // Returns the eFileTypes part of the key
            }
        }
        return null;  // Return null if the filename is not found
    }

    /**
     * Removes a file from the database if it exists, determining the file type internally.
     * @param fileName The name of the file to remove.
     * @return true if the file was removed, false if it did not exist.
     */
    public boolean removeFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for removeFile.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        // First, determine the file type
        eFileTypes fileType = determineFileType(fileName);
        if (fileType == null) {
            logger.warn("Attempted to remove '{}' but its type could not be determined or it does not exist in the database.", fileName);
            return false;  // File type couldn't be determined or file does not exist
        }

        // If the file type is determined, attempt to remove it
        boolean removed = this.fileNameToNodeIP.remove(Map.entry(fileName, fileType)) != null;
        if (removed) {
            logger.info("File '{}' of type '{}' was removed from the database.", fileName, fileType);
        } else {
            logger.warn("Attempted to remove '{}' of type '{}' but it did not exist in the database.", fileName, fileType);
        }
        return removed;
    }

    /**
     * Checks if a file exists in the database by determining the file type internally.
     * @param fileName The name of the file.
     * @return true if the file exists, false otherwise.
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for fileExists.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        eFileTypes filetype = determineFileType(fileName);
        return filetype != null;
    }

    /**
     * Gets all files along with their file types and node IPs from the database.
     * @return a Map of file names with their file types to node IPs.
     */
    public Map<Map.Entry<String, eFileTypes>, String> getAllFiles() {
        return new ConcurrentHashMap<>(fileNameToNodeIP);
    }

    /**
     * Gets the file type of a specific file.
     * @param fileName The name of the file.
     * @return The file type (Local or Replicated) or null if the file does not exist.
     */
    public eFileTypes getFileType(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Invalid file name provided for getFileType.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        
        return determineFileType(fileName);
    }

}
