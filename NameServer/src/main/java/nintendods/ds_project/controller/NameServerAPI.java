package nintendods.ds_project.controller;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.model.NameServerDatabase;
import nintendods.ds_project.model.NodeModel;
import nintendods.ds_project.model.ResponseObject;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;

@RestController
public class NameServerAPI {

    private final NameServerDatabase nodesDatabase = new NameServerDatabase();
    private final JsonConverter jsonConverter = new JsonConverter("DataBase.json");

    //Not yet implemented
    //@GetMapping("/files")
    //public ResponseEntity<NameServerDatabase> getNodes() { return ResponseEntity.status(HttpStatus.OK).body(nodesDatabase); }

    @GetMapping("/files/{file_name}")
    public ResponseEntity<String> getFileAddressByName(@PathVariable("file_name") String name) {
        InetAddress ip = nodesDatabase.getClosestNodeIP(name);

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

        // TODO: lijkt replicatie van onderstaande? als ID al bestaat, dan bestaat de node ook.
        //  Of de node heeft dezelfde naam als een andere node wat problematisch is.
        if (nodesDatabase.exists(newNode)) {
            response.setMessage("Item already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(jsonConverter.toJson(response));
        }
        //TODO: check ofdat we mss beter checken op andere velden van existance in de datatbase.
        //if (nodesDatabase.exists(newNode)) {
        //    response.setMessage(String.format("Item with id = %d already exists", newNode.getId()));
        //    return ResponseEntity.status(HttpStatus.CONFLICT).body(jsonConverter.toJson(response));
        //}

        try {
            nodesDatabase.addNode(newNode);
        }
        catch (NameServerFullExeption ex){
            System.out.println(ex);
            response.setMessage("The database is full. Max amount of nodes are reached");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonConverter.toJson(response));
        }

        return ResponseEntity.status(HttpStatus.OK).body(jsonConverter.toJson(response));
    }
}
