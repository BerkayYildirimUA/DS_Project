package nintendods.ds_project.controller;

import nintendods.ds_project.model.ClientNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Management")
public class ClientManagementAPI {

    @Autowired
    private ClientNode node;
    private static final Logger logger = LoggerFactory.getLogger(ClientManagementAPI.class);

    @PutMapping("/nextNodeID/")
    public ResponseEntity<String> changeNextNode(@RequestParam("ID") int ID){
        logger.info("Request to change next Node to {}", ID);
        node.setNextNodeId(ID);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/prevNodeID/")
    public ResponseEntity<String> changePrevNode(@RequestParam("ID") int ID){
        logger.info("Request to change prev Node to {}", ID);
        node.setPrevNodeId(ID);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/name/")
    public ResponseEntity<String> getNodeInformation() {
        logger.info("Requesting Node information");
        String nodeInformation = node.toString();
        return ResponseEntity.status(HttpStatus.OK).body(nodeInformation);
    }
}
