package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.database.eFileTypes;
import nintendods.ds_project.utility.NameToHash;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileManagementService {
    private static final Logger logger = LoggerFactory.getLogger(FileManagementService.class);
    private final FileDB fileDB;

    public FileManagementService() {
        this.fileDB = FileDBService.getFileDB();
    }

    /**
     * Start service by replicating all local files to the designated nodes in system Y.
     */
    public void startReplication() {
        // Assuming a method to get all files and their current locations
        fileDB.getAllFiles().forEach((file, nodeIP) -> {
            if (fileDB.getFileType(file.getKey()) == eFileTypes.Local) {
                replicateFile(file.getKey(), nodeIP);
            }
        });
    }

    /**
     * Replicate a file to another node.
     * @param fileName the name of the file to replicate.
     * @param sourceNodeIP IP address of the node where the file is currently stored.
     */
    private void replicateFile(String fileName, String sourceNodeIP) {
        // Determine the target node IP somehow, possibly through some load balancing or hashing mechanism
        String targetNodeIP = determineTargetNodeIP(fileName);
        // Simulate file transfer
        logger.info("Replicating file '{}' from '{}' to '{}'", fileName, sourceNodeIP, targetNodeIP);
        // Update fileDB to reflect the new replicated file's location
        fileDB.addOrUpdateFile(fileName, eFileTypes.Replicated, targetNodeIP);
    }

    /**
     * Handle the addition or deletion of files in a node.
     * @param fileName the name of the file that was added or deleted.
     * @param nodeIP the node where the change occurred.
     * @param isAdded true if the file was added, false if deleted.
     */
    public void updateFileState(String fileName, String nodeIP, boolean isAdded) {
        if (isAdded) {
            fileDB.addOrUpdateFile(fileName, eFileTypes.Local, nodeIP);
            replicateFile(fileName, nodeIP);
        } else {
            fileDB.removeFile(fileName);
        }
    }

    /**
     * Prepare for system shutdown by either transferring or deleting files.
     */
    public void prepareForShutdown() {
        // Assume method to get all files that need to be transferred or deleted
        fileDB.getAllFiles().forEach((file, nodeIP) -> {
            if (fileDB.getFileType(file) == eFileTypes.Local) {
                logger.info("Transferring or deleting file '{}' from '{}'", file, nodeIP);
                // Logic to either transfer or delete the file
                transferOrDeleteFile(file, nodeIP);
            }
        });
    }

    /**
     * Determines the target node IP for file replication.
     * @param fileName the name of the file to be replicated.
     * @return the IP address of the target node.
     */
    private String determineTargetNodeIP(String fileName) {
        // Implement logic to determine the target node IP based on fileName or other criteria
        return "192.168.1.2";  // Example IP
    }

    /**
     * Either transfers or deletes a file depending on system state and file importance.
     * @param fileName the name of the file to handle.
     * @param currentIP the IP address of the node currently holding the file.
     */
    private void transferOrDeleteFile(String fileName, String currentIP) {
        // Implement logic to decide whether to transfer or delete the file
        // This is just a placeholder example
        if (Math.random() > 0.5) {
            String targetNodeIP = determineTargetNodeIP(fileName);
            logger.info("Transferring '{}' from '{}' to '{}'", fileName, currentIP, targetNodeIP);
            fileDB.addOrUpdateFile(fileName, eFileTypes.Local, targetNodeIP);
        } else {
            logger.info("Deleting file '{}' from '{}'", fileName, currentIP);
            fileDB.removeFile(fileName);
        }
    }

    /**
     * Calculate and return hash values for all files in the fileDB using a custom hash function.
     * @return A map of filenames to their respective hash values as integers within the range of 0 to 32768.
     */
    public Map<String, Integer> calculateFileHashes() {
        Map<String, Integer> fileHashes = new HashMap<>();
        fileDB.getAllFiles().keySet().forEach(entry -> {
            String fileName = entry.getKey();
            int hash = NameToHash.convert(fileName);
            fileHashes.put(fileName, hash);
        });
        return fileHashes;
    }
}
