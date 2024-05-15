package nintendods.ds_project.controller;

import java.lang.reflect.Type;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.reflect.TypeToken;

import nintendods.ds_project.Client;
import nintendods.ds_project.agent.sync.SyncAgent;
import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.JsonConverter;

/**
 * REST controller for managing agents
 * Provides endpoint for tossing sync agent
 */
@RestController
@RequestMapping("/api/agent")
public class ClientAgentAPI {

    @Autowired
    ConfigurableApplicationContext context;

    protected static final Logger logger = LoggerFactory.getLogger(ClientAgentAPI.class);

    @PostMapping("sync")
    public ResponseEntity<Boolean> recieveSyncAgent(@RequestBody String syncAgentFiles) {
        logger.debug("Recieve a sync agent");

        if (syncAgentFiles != null) {

            processSyncAgent(syncAgentFiles); //Async so will continue

            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @Async
    private void processSyncAgent(String syncAgentFiles) {
        try {
            if (syncAgentFiles != null) {
                SyncAgent agent = new SyncAgent((Map<String, Boolean>) new JsonConverter().toObject(syncAgentFiles, new TypeToken<Map<String, Boolean>>() {}.getType()));
                Thread t = new Thread(agent);
                t.start();

                t.join();

                logger.debug("Start async tossing agent");
                // Find the next node IP.
                Client client = context.getBean(Client.class);
                int nextNodeId = client.getNode().getNextNodeId();
                String ipNextNode = ApiUtil.NameServer_GET_NodeIPfromID(nextNodeId);

                // Send
                ApiUtil.sendSyncAgent(ipNextNode, ClientNodeConfig.API_PORT, agent); // Will return if next node is finished.
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}