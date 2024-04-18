package nintendods.ds_project.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nintendods.ds_project.helper.NameToHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NodeModel extends ABaseNode {

    private int id;
    private List<String> localFiles;
    private List<String> remoteFiles;
    private NameServerDatabase nameServer; // Integration with NameServerDatabase

    private static final Logger logger = LoggerFactory.getLogger(NodeModel.class);

    public NodeModel(int id, InetAddress address, int port) {
        super(address, port);
        this.id = id;
        this.localFiles = new ArrayList<>();
        this.remoteFiles = new ArrayList<>();
        this.nameServer = new NameServerDatabase(); // Initialize NameServerDatabase
    }

    public int getId() {
        return id;
    }

    public List<String> getLocalFiles() {
        return localFiles;
    }

    public List<String> getRemoteFiles() {
        return remoteFiles;
    }

    public void createFile(String fileName) {
        // Perform logic to create file
        // Communicate with the naming server here to register the file
        try {
            localFiles.add(fileName);
            logger.info("File '{}' created successfully", fileName);
        } catch (Exception e) {
            logger.error("Error occurred while creating file '{}': {}", fileName, e.getMessage());
            System.err.println("Error occurred while creating file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileName) {
        // Perform logic to delete file
        // Communicate with the naming server here to unregister the file
        try {
            localFiles.remove(fileName);
            logger.info("File '{}' deleted successfully", fileName);
        } catch (Exception e) {
            logger.error("Error occurred while deleting file '{}': {}", fileName, e.getMessage(), e);
            System.err.println("Error occurred while deleting file: " + e.getMessage());
        }
    }

    // Algorithm for file names conversion to hash value
    public int convertFileNameToHash(String fileName) {
        try {
            return NameToHash.convert(fileName);
        } catch (Exception e) {
            logger.error("Error occurred while converting file name to hash: {}", e.getMessage(), e);
            System.err.println("Error occurred while converting file name to hash: " + e.getMessage());
            return 0; // or handle the error in an appropriate way
        }
    }

    // Save to JSON
    public void saveToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("node_state.json")) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            logger.error("Error occurred while saving node state to JSON: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // API methods
    public void createFileApi(String fileName) {
        createFile(fileName);
        // Perform API call for updating node state
        saveToJson(); // Save node state after creating file
        logger.info("File '{}' created via API", fileName);
    }

    public void deleteFileApi(String fileName) {
        deleteFile(fileName);
        // Perform API call for updating node state
        saveToJson(); // Save node state after deleting file
        logger.info("File '{}' deleted via API", fileName);
    }

    public String getFile(String fileName) {
        // Search for the file locally
        if (localFiles.contains(fileName) || remoteFiles.contains(fileName)) {
            // If found locally, return the file content
            return "Content of the file: " + fileName;
        }

        // If not found locally, try to fetch from the naming server
        String fileContent = fetchFileFromNamingServer(fileName);
        if (!"File not found".equals(fileContent)) {
            logger.info("File '{}' fetched from naming server", fileName);
        }
        return fileContent;
    }

    // Method to fetch file from the naming server
    private String fetchFileFromNamingServer(String fileName) {
        // Placeholder implementation indicating that the file was fetched from the naming server
        InetAddress nodeAddress = nameServer.getNodeIP(NameToHash.convert(fileName));
        if (nodeAddress != null) {
            // If node IP is found, you can implement further logic to fetch the file content
            return "Content of the file fetched from naming server: " + fileName;
        } else {
            logger.warn("File '{}' not found in naming server", fileName);
            return "File not found";
        }
    }
}
