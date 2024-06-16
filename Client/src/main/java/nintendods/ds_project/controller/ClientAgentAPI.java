package nintendods.ds_project.controller;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.service.AgentService;
import nintendods.ds_project.service.FailureAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.reflect.TypeToken;

import nintendods.ds_project.Client;
import nintendods.ds_project.utility.JsonConverter;
import org.w3c.dom.Node;

/**
 * REST controller for managing agents
 * Provides endpoint for tossing sync agent
 */
@RestController
@RequestMapping("/api/agent")
public class ClientAgentAPI {

    @Autowired
    ConfigurableApplicationContext context;

    @Autowired
    private AgentService agentService;

    protected static final Logger logger = LoggerFactory.getLogger(ClientAgentAPI.class);
    private JsonConverter jsonConv = new JsonConverter();

    @GetMapping("sync")
    public ResponseEntity<String> requestSyncAgent() {
        logger.debug("Recieve a sync agent file list call");

        Client client = context.getBean(Client.class);
        Type syncAgentFileListType = new TypeToken<HashMap<String, Boolean>>() {}.getType();
        
        nintendods.ds_project.service.SyncAgent syncAgent = client.getSyncAgent();
        if (syncAgent != null) {
            return ResponseEntity.ok().body(jsonConv.toJson(syncAgent.getFiles(), syncAgentFileListType));
        }
        return ResponseEntity.ok().body("");
    }

    @PutMapping("failure")
    public ResponseEntity<String> receiveFailureAgent(@RequestBody byte[] agentData){

        try {
            FailureAgent agent = (FailureAgent) FailureAgent.deserialize(agentData);
            Future<FailureAgent> future = agentService.runAgent(agent);
            future.get(); // Wait for the agent to complete execution

            return ResponseEntity.ok("FailureAgent received and executed");
        } catch (InterruptedException | ExecutionException  e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        }
    }

    @PostMapping("failure/{ID}")
    public ResponseEntity<String> creatFailureAgent(@PathVariable("ID") String failedNodeID){
        try {
            String thisNode = String.valueOf(context.getBean(Client.class).getNode().getId());
            FailureAgent agent = new FailureAgent(failedNodeID, thisNode);
            Future<FailureAgent> future = agentService.runAgent(agent);
            future.get(); // Wait for the agent to complete execution
            return ResponseEntity.ok("FailureAgent received and executed");
        } catch (InterruptedException | ExecutionException  e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        }
    }
}
