package nintendods.ds_project.controller;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.model.NodeModel;
import nintendods.ds_project.model.message.ResponseObject;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.service.NodeDBService;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;

@RestController
public class NameServerAPI {
    JsonConverter jsonConverter = new JsonConverter("Database.json");
    NodeDB nodeDB = NodeDBService.getNodeDB();

    @GetMapping("/files/{id}")
    public ResponseEntity<String> getFileById(@PathVariable("id") int id) {
        NodeModel node = (NodeModel) nodeDB.getNodefromID(id);
        ResponseObject<NodeModel> response = new ResponseObject<>(node);

        if (node != null)   return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(response));
        else                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/files/{file_name}/address")
    public ResponseEntity<String> getFileAddressByName(@PathVariable("file_name") String name) {
        InetAddress ip = nodeDB.getClosestNodeIP(name);

        if (ip != null) return ResponseEntity.status(HttpStatus.OK).body(ip.getHostAddress());
        else            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/files")
    public ResponseEntity<String> postFile(@RequestBody NodeModel newNode) {
        ResponseObject<NodeModel> response = new ResponseObject<>(newNode);

        if (newNode == null) {
            response.setMessage("No item was given in body");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonConverter.toJson(response));
        }

        //TODO: check ofdat we mss beter checken op andere velden van existance in de datatbase.
        // Enkel de id is uniek. De naam kan hetzelfde zijn en de port/ip moeten wij toekenen,
        // dus erop filteren is nuteloos want als dat niet juist is,
        // is er gewoon iets mis bij de allocatie en moet dat daar opgelost worden.
        if (nodeDB.exists(newNode)) {
            response.setMessage(String.format("Item with id = %d already exists", newNode.getId()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jsonConverter.toJson(response));
        }

        try {
            nodeDB.addNode(newNode);
        }
        catch (NameServerFullExeption ex){
            System.out.println(ex);
            response.setMessage("The database is full. Max amount of nodes are reached");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonConverter.toJson(response));
        }

        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(response));
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<String> deleteFileById(@PathVariable("id") int id) {
        NodeModel node = (NodeModel) nodeDB.getNodefromID(id);
        ResponseObject<NodeModel> response = new ResponseObject<>(node);

        if (node == null) {
            response.setMessage(String.format("Item with id = %d does not exists", node.getId()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonConverter.toJson(response));
        }

        nodeDB.deleteNode(node);
        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(response));
    }
}
