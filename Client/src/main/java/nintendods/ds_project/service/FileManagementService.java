package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.database.eFileTypes;
import nintendods.ds_project.utility.JsonConverter;
import nintendods.ds_project.utility.NameToHash;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileManagementService {
    private static final Logger logger = LoggerFactory.getLogger(FileManagementService.class);
    //private final NSAPIService nsapiService;
    private final JsonConverter jsonConverter;
    private final FileDB fileDB;

    public FileManagementService() {
        this.fileDB = FileDBService.getFileDB();
        //this.nsapiService = nsapiService;
        this.jsonConverter = new JsonConverter("fileHashes.json"); // Assumes that you may optionally want to save hashes to a file
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

    /**
     * Reports file hashes to the naming server using the NSAPIService.
     */
    /*
    public void reportFileHashesToNamingServer() {
        Map<String, Integer> fileHashes = calculateFileHashes();
        String jsonPayload = jsonConverter.toJson(fileHashes); // Convert hashes map to JSON
        String response = nsapiService.executePost_json("/api/report-hashes", jsonPayload); // Use your custom NSAPIService to send the JSON
        logger.info("Response from naming server: {}", response);
    }*/

    // For test only:
    public FileDB getFileDB() {
        return this.fileDB;
    }
}
