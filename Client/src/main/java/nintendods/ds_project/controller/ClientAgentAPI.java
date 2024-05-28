package nintendods.ds_project.controller;
import java.lang.reflect.Type;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.reflect.TypeToken;

import nintendods.ds_project.Client;
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
}
