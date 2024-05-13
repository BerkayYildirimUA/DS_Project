package nintendods.ds_project.controller;

import nintendods.ds_project.Exeptions.IDTakenExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.ResponseObject;
import nintendods.ds_project.service.NodeDBService;
import nintendods.ds_project.service.TCPService;
import nintendods.ds_project.utility.JsonConverter;
import nintendods.ds_project.utility.NameToHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.TreeMap;

@RestController
public class NameServerAPI {
    private final Logger logger = LoggerFactory.getLogger(NameServerAPI.class);
    JsonConverter jsonConverter = new JsonConverter("Database.json");
    NodeDB nodeDB = NodeDBService.getNodeDB();
    TCPService tcpService = TCPService.getTcpService();

    private void printAndLog(String message) {
        // logger.info(message);
        System.out.println(message);
    }

    @GetMapping("/files/{file_name}")
    public ResponseEntity<String> getFileAddressByName(@PathVariable("file_name") String name) {
        printAndLog("GET:\t IP by file ID");
        int closestId = nodeDB.getClosestIdFromName(name);
        int id = NameToHash.convert(name);
        if (closestId > id) {
            // If closestId is higher, than it is the id of the next node
            closestId = nodeDB.getPreviousId(closestId);
        }
        String ip = nodeDB.getIpFromId(closestId);
        printAndLog("GET:\t IP=" + ip);

        if (ip != null) return ResponseEntity.status(HttpStatus.OK).body(ip);
        else            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/node/{id}")
    public ResponseEntity<String> getNodeIpFromId(@PathVariable("id") int id) {
        printAndLog("GET:\t IP by ID");
        String ip = nodeDB.getIpFromId(id);
        printAndLog("GET:\t IP=" + ip);

        if (ip != null && !ip.isEmpty()) return ResponseEntity.status(HttpStatus.OK).body(ip);
        else            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/nodes")
    public ResponseEntity<String> postNode(@RequestBody ClientNode newNode) {
        printAndLog("POST: Add client node to database");
        ResponseObject<ClientNode> badResponse = new ResponseObject<>(newNode);

        if (newNode == null) {
            printAndLog("Error: " + HttpStatus.NO_CONTENT +  " - No item was given in body");
            badResponse.setMessage("No item was given in body");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonConverter.toJson(badResponse));
        }

        if (nodeDB.exists(newNode.getId())) {
            printAndLog("Error: " + HttpStatus.CONFLICT +  " - Item already exists");
            badResponse.setMessage(String.format("Item with id = %d or name "+ newNode.getName() +" already exists", newNode.getId()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jsonConverter.toJson(badResponse));
        }

        try {
            nodeDB.addNode(newNode.getName(), newNode.getAddress().getHostAddress());
        }
        catch (IDTakenExeption ex){
            printAndLog("Error: " + HttpStatus.INTERNAL_SERVER_ERROR +  " - The database is full.");
            badResponse.setMessage("The database is full. Max amount of nodes are reached");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonConverter.toJson(badResponse));
        }

        Map<Integer, String> params = new TreeMap<>();
        params.put(nodeDB.getClosestIdFromName(newNode.getName()), nodeDB.getClosestIpFromName(newNode.getName()));
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(params));
    }

    @PatchMapping("/nodes/{id}/error")
    public ResponseEntity<String> endError(@PathVariable("id") int id) {
        printAndLog("Node has been updated");
        tcpService.stopClient(id);

        ResponseObject<Integer> response = new ResponseObject<>(id);
        response.setMessage(String.format("Item with id = %d has been updated", id));
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(response));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<String> deleteFileById(@PathVariable("id") int id) {
        printAndLog("DELETE: Remove client node from database by ID");
        ResponseObject<Integer> response = new ResponseObject<>(id);

        if (!nodeDB.exists(id)) {
            printAndLog("Error: " + HttpStatus.CONFLICT +  " - Item already exists");
            response.setMessage(String.format("Item with id = %d does not exists", id));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        nodeDB.deleteNode(id);
        logger.info("{} has been deleted from the server", id);
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(id));
    }

    @DeleteMapping("/nodes/{id}/error")
    public ResponseEntity<String> deleteDueToError(@PathVariable("id") int id) {
        printAndLog("DELETE: Remove client node due to error");

        ResponseObject<Integer> response = new ResponseObject<>(id);

        if (!nodeDB.exists(id)) {
            printAndLog("Error: " + HttpStatus.CONFLICT +  " - Item already exists");
            response.setMessage(String.format("Item with id = %d does not exists", id));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        int prevNode = nodeDB.getPreviousId(id);
        int nextNode = nodeDB.getNextId(id);

        /* Code for error */
        if (prevNode == nextNode) {
            // Next and previous node are the same
            Thread neighborNodeThread = tcpService.createTCPThread(prevNode, id, prevNode);
            neighborNodeThread.start();
        } else {
            // Previous node
            Thread previousNodeThread = tcpService.createTCPThread(prevNode, id, nextNode);
            previousNodeThread.start();
            // Next node
            Thread nextNodeThread = tcpService.createTCPThread(nextNode, id, prevNode);
            nextNodeThread.start();
        }

        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(id));
    }

    @DeleteMapping("/nodes/{id}/shutdown")
    public ResponseEntity<String> deleteDueToShutdown(@PathVariable("id") int id) {
        printAndLog("DELETE: Remove client node due to shutdown");
        ResponseObject<Integer> response = new ResponseObject<>(id);

        /* Code for shutdown */

        if (!nodeDB.exists(id)) {
            printAndLog("Error: " + HttpStatus.CONFLICT +  " - Item already exists");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        nodeDB.deleteNode(id);
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(id));
    }
}
