package nintendods.ds_project.controller;

import com.google.gson.Gson;
import nintendods.ds_project.utility.ClosestIdHelper;
import nintendods.ds_project.model.NodeModel;
import nintendods.ds_project.model.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DS_API {

    private final List<NodeModel> nodes = new ArrayList<>();

    @GetMapping("/files")
    public ResponseEntity<List<NodeModel>> getNodes() { return ResponseEntity.status(HttpStatus.OK).body(nodes); }

    @GetMapping("/files/{file_name}")
    public ResponseEntity<String> getFileAddressByName(@PathVariable("file_name") String name) {
        InetAddress ip = ClosestIdHelper.getClosestNode(nodes, name).getAddress();
        if (ip != null) return ResponseEntity.status(HttpStatus.OK).body(ip.getHostAddress());
        else            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/files")
    public ResponseEntity<String> postFile(@RequestBody NodeModel newNode) {
        ResponseObject<NodeModel> response = new ResponseObject<>(newNode);
        Gson gson = new Gson();

        if (newNode == null) {
            response.setMessage("No item was given in body");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(gson.toJson(response));
        }

        if (nodes.contains(newNode)) {
            response.setMessage("Item already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(gson.toJson(response));
        }

        if (nodes.stream().anyMatch(node -> node.getId() == newNode.getId())) {
            response.setMessage(String.format("Item with id = %d already exists", newNode.getId()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(gson.toJson(response));
        }

        nodes.add(newNode);
        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(response));
    }
}