package nintendods.ds_project.controller;
import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.service.FileDBService;
import nintendods.ds_project.utility.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing file metadata in a distributed system.
 * Provides endpoints for adding, retrieving, and deleting file information.
 */
@RestController
@RequestMapping("/api/files")
public class ClientAPI {
    private static final Logger logger = LoggerFactory.getLogger(ClientAPI.class);
    private final JsonConverter jsonConverter = new JsonConverter();
    private final FileDB fileDB = FileDBService.getFileDB();

    @GetMapping("/{fileName}")
    public ResponseEntity<String> getFileLocation(@PathVariable("fileName") String fileName) {
        logger.debug("Request to retrieve location for file: {}", fileName);
        try {
            String fileLocation = fileDB.getFileLocation(fileName);
            if (fileLocation != null) {
                logger.info("File location retrieved successfully for file: {}", fileName);
                return ResponseEntity.ok(jsonConverter.toJson(fileLocation));
            } else {
                logger.warn("File location not found for file: {}", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson("File not found."));
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error retrieving file location: {}", e.getMessage());
            return ResponseEntity.badRequest().body(jsonConverter.toJson(e.getMessage()));
        }
    }

    @PostMapping("/")
    public ResponseEntity<String> addFile(@RequestParam("fileName") String fileName, @RequestParam("nodeIP") String nodeIP) {
        logger.debug("Attempting to add/update file: {} at {}", fileName, nodeIP);
        try {
            fileDB.addOrUpdateFile(fileName, nodeIP);
            logger.info("File added/updated successfully: {}", fileName);
            return ResponseEntity.status(HttpStatus.CREATED).body(jsonConverter.toJson("File added/updated successfully."));
        } catch (IllegalArgumentException e) {
            logger.error("Error adding/updating file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(jsonConverter.toJson(e.getMessage()));
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable("fileName") String fileName) {
        logger.debug("Request to delete file: {}", fileName);
        try {
            boolean removed = fileDB.removeFile(fileName);
            if (removed) {
                logger.info("File deleted successfully: {}", fileName);
                return ResponseEntity.ok(jsonConverter.toJson("File deleted successfully."));
            } else {
                logger.warn("File not found for deletion: {}", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson("File not found."));
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(jsonConverter.toJson(e.getMessage()));
        }
    }
}