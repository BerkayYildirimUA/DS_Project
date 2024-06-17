package nintendods.ds_project.controller;

import com.google.gson.reflect.TypeToken;
import nintendods.ds_project.Client;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.service.AgentService;
import nintendods.ds_project.service.FailureAgent;
import nintendods.ds_project.service.FileTransceiverService;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * REST controller for managing agents
 * Provides endpoint for tossing sync agent
 */
@RestController
@RequestMapping("/api/agent")
public class ClientAgentAPI {

    protected static final Logger logger = LoggerFactory.getLogger(ClientAgentAPI.class);
    @Autowired
    ConfigurableApplicationContext context;
    @Autowired
    private AgentService agentService;
    private final JsonConverter jsonConv = new JsonConverter();

    @GetMapping("sync")
    public ResponseEntity<String> requestSyncAgent() {
        logger.debug("Recieve a sync agent file list call");

        Client client = context.getBean(Client.class);
        Type syncAgentFileListType = new TypeToken<HashMap<String, Boolean>>() {
        }.getType();

        nintendods.ds_project.service.SyncAgent syncAgent = client.getSyncAgent();
        if (syncAgent != null) {
            return ResponseEntity.ok().body(jsonConv.toJson(syncAgent.getFiles(), syncAgentFileListType));
        }
        return ResponseEntity.ok().body("");
    }

    @PutMapping("failure")
    public ResponseEntity<String> receiveFailureAgent(@RequestBody byte[] agentData) {

        logger.info("received failure agent");

        try {
            FailureAgent agent = (FailureAgent) FailureAgent.deserialize(agentData);
            logger.info("deserialized failure agent");
            ClientNode node = context.getBean(Client.class).getNode();
            Optional<FailureAgent> agentOptional = agent.setCurrentNodeId(String.valueOf(node.getId()));
            if (agentOptional.isEmpty()) {
                logger.info("FailureAgent returned to start");
                return ResponseEntity.ok("FailureAgent returned to start");
            }

            agent = agentOptional.get();
            agent.setFileTransceiverService(context.getBean(Client.class).getFileTransceiver());
            logger.info("run failure agent");
            Future<FailureAgent> future = agentService.runAgent(agentOptional.get());
            future.get();
            logger.info("failure agent done running");

            FailureAgent finalAgent = agent;
            Thread sendToNextNode = new Thread(() -> {
                try {
                    logger.info("send failure agent to next node");
                    ApiUtil.Client_PUT_sendFailureAgent(finalAgent, String.valueOf(node.getPrevNodeId()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            sendToNextNode.start();
            return ResponseEntity.ok("FailureAgent received and executed");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        }
    }

    @PostMapping("failure")
    public ResponseEntity<String> createFailureAgent(@RequestParam("ID") String failedNodeID) {
        logger.info("create failure agent request");
        try {
            String thisNode = String.valueOf(context.getBean(Client.class).getNode().getId());
            FileTransceiverService fileTransceiverService = context.getBean(Client.class).getFileTransceiver();
            FailureAgent agent = new FailureAgent(failedNodeID, thisNode, fileTransceiverService);
            logger.info("created failure agent");
            Future<FailureAgent> future = agentService.runAgent(agent);
            logger.info("run failure agent");
            future.get(); // Wait for the agent to complete execution
            logger.info("send failure agent");
            ClientNode node = context.getBean(Client.class).getNode();

            Thread sendToNextNode = new Thread(() -> {
                try {
                    logger.info("send failure agent to next node");
                    ApiUtil.Client_PUT_sendFailureAgent(agent, String.valueOf(node.getPrevNodeId()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            sendToNextNode.start();
            return ResponseEntity.ok("FailureAgent received and executed");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing FailureAgent: " + e.getMessage());
        }
    }
}
