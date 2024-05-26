package nintendods.ds_project.controller;

import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.service.FileDBService;
import nintendods.ds_project.utility.JsonConverter;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for managing file metadata in a distributed system.
 * Provides endpoints for adding, retrieving, and deleting file information.
 */
@RestController
@RequestMapping("/api/files")
public class ClientFileAPI {
    private static final Logger logger = LoggerFactory.getLogger(ClientFileAPI.class);
    private final JsonConverter jsonConverter = new JsonConverter();
    private final FileDB fileDB = FileDBService.getFileDB();

    @GetMapping("")
    public ResponseEntity<String> getAllFiles() {
        logger.debug("Request of all files on local system");
        try {
            List<AFile> files = fileDB.getFiles();
            if (!files.isEmpty()) {
                logger.info("Transfering files to requester");
                return ResponseEntity.ok(jsonConverter.toJson(files));
            } else {
                logger.warn("No files present");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error request of all files");
            return ResponseEntity.badRequest().body("");
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<String> getFileLocation(@PathVariable("fileName") String fileName) {
        logger.debug("Request to retrieve location for file: {}", fileName);
        try {
            Optional<AFile> file = fileDB.getFile(fileName);
            if (file.isPresent()) {
                logger.info("File location retrieved successfully for file: {}", fileName);
                return ResponseEntity.ok(jsonConverter.toJson(file.get().getAbsolutePath()));
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
    public ResponseEntity<String> addFile(@RequestBody AFile file) {
        logger.debug("Attempting to add/update file: {}", file);
        try {
            fileDB.addOrUpdateFile(file);
            logger.info("File added/updated successfully: {}", file);
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

    @PutMapping("/{fileName}/downloadLocation")
    public ResponseEntity<String> changeOwner(@PathVariable("fileName") String fileName, @RequestBody String absolutePath, @RequestBody int nodeID){
        Optional<AFile> fileOptional = fileDB.getFileByAbsolutePath(absolutePath);

        if (fileOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson("File not found"));
        }

        AFile file = fileOptional.get();
        file.setDownloadLocation(Integer.toString(nodeID));
        logger.info("changed download location of:" + fileName +"\n to: " + nodeID);
        return ResponseEntity.ok().body(jsonConverter.toJson("file changes successfully"));
    }
}