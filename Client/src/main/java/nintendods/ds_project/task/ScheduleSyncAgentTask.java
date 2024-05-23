package nintendods.ds_project.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import nintendods.ds_project.Client;
import nintendods.ds_project.service.SyncAgent;

@Component
public class ScheduleSyncAgentTask {

    @Autowired
    ConfigurableApplicationContext context;
    // private SyncAgent syncAgent;

    // public ScheduleSyncAgentTask(){
    //     syncAgent = new SyncAgent((ApplicationContext)context);
    // }

    private static final Logger log = LoggerFactory.getLogger(ScheduleSyncAgentTask.class);

    @Scheduled(fixedRate = 5000) // in milliseconds so 5 seconds total
    public void reportCurrentTime() {
        Client client = context.getBean(Client.class);
        SyncAgent syncAgent = client.getSyncAgent();

        // Agent has been created
        if (syncAgent != null) {
            log.info("Fire agent at node {}", client.getNode().getName());

            // run the agent process
            syncAgent.run();
        }
    }
}