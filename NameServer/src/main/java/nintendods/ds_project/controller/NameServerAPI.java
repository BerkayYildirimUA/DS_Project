package nintendods.ds_project.controller;

import nintendods.ds_project.Exeptions.IDTakenExeption;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.ResponseObject;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.service.NodeDBService;
import nintendods.ds_project.service.TCPClient;
import nintendods.ds_project.utility.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@RestController
public class NameServerAPI {
    private final Logger logger = LoggerFactory.getLogger(NameServerAPI.class);
    JsonConverter jsonConverter = new JsonConverter("Database.json");
    NodeDB nodeDB = NodeDBService.getNodeDB();

    @GetMapping("/files/{file_name}")
    public ResponseEntity<String> getFileAddressByName(@PathVariable("file_name") String name) {
        logger.info("GET: File by ID");
        String ip = nodeDB.getClosestIpFromName(name);

        if (ip != null) return ResponseEntity.status(HttpStatus.OK).body(ip);
        else            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/nodes")
    public ResponseEntity<String> postNode(@RequestBody ClientNode newNode) {
        logger.info("POST: Add client node to database");
        ResponseObject<ClientNode> badResponse = new ResponseObject<>(newNode);

        if (newNode == null) {
            badResponse.setMessage("No item was given in body");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonConverter.toJson(badResponse));
        }

        if (!nodeDB.exists(newNode.getAddress().getHostAddress())) {
            badResponse.setMessage(String.format("Item with id = %d or name "+ newNode.getName() +" already exists", newNode.getId()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jsonConverter.toJson(badResponse));
        }

        try {
            nodeDB.addNode(newNode.getName(), newNode.getAddress().getHostAddress());
        }
        catch (IDTakenExeption ex){
            System.out.println(ex);
            badResponse.setMessage("The database is full. Max amount of nodes are reached");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonConverter.toJson(badResponse));
        }

        Map<Integer, String> params = new TreeMap<>();
        params.put(nodeDB.getClosestIdFromName(newNode.getName()), nodeDB.getClosestIpFromName(newNode.getName()));
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(params));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<String> deleteFileById(@PathVariable("id") int id) {
        logger.info("DELETE: Remove client node from database by ID");
        ResponseObject<Integer> response = new ResponseObject<>(id);

        if (!nodeDB.exists(id)) {
            response.setMessage(String.format("Item with id = %d does not exists", id));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        nodeDB.deleteNode(id);
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(id));
    }

    @DeleteMapping("/nodes/{id}/error")
    public ResponseEntity<String> deleteDueToError(@PathVariable("id") int id) {
        logger.info("DELETE: Remove client node due to error");
        System.out.println("DELETE: Remove client node due to error");
        ResponseObject<Integer> response = new ResponseObject<>(id);

        if (!nodeDB.exists(id)) {
            System.out.println(String.format("Item with id = %d does not exists", id));
            response.setMessage(String.format("Item with id = %d does not exists", id));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        /* Code for error */
        Thread previousNodeThread = new Thread(() -> {
            TCPClient client = new TCPClient();
            try {
                System.out.println("Start connection with previous nodes");
                logger.info("Start connection with previous nodes");
                client.connect(nodeDB.getIpFromId(nodeDB.getPreviousId(id)));
            } catch (IOException e) {
                System.out.println("Failed to connect with previous nodes");
                logger.info("Failed to connect with previous nodes");
                throw new RuntimeException(e);
            }
        });
        previousNodeThread.start();
        Thread nextNodeThread = new Thread(() -> {
            TCPClient client = new TCPClient();
            try {
                System.out.println("Start connection with next nodes");
                logger.info("Start connection with next nodes");
                client.connect(nodeDB.getIpFromId(nodeDB.getNextId(id)));
            } catch (IOException e) {
                System.out.println("Failed to connect with next nodes");
                logger.info("Failed to connect with next nodes");
                throw new RuntimeException(e);
            }
        });
        nextNodeThread.start();

        System.out.println("Delete node");
        nodeDB.deleteNode(id);
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(id));
    }

    @DeleteMapping("/nodes/{id}/shutdown")
    public ResponseEntity<String> deleteDueToShutdown(@PathVariable("id") int id) {
        logger.info("DELETE: Remove client node due to shutdown");
        ResponseObject<Integer> response = new ResponseObject<>(id);

        /* Code for shutdown */

        if (!nodeDB.exists(id)) {
            response.setMessage(String.format("Item with id = %d does not exists", id));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        nodeDB.deleteNode(id);
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(id));
    }
}
