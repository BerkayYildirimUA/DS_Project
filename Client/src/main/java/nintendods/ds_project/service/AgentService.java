package nintendods.ds_project.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class AgentService {

    public Future<FailureAgent> runAgent(FailureAgent agent) {
        return CompletableFuture.supplyAsync(() -> {
            Thread agentThread = new Thread(agent);
            agentThread.start();
            try {
                agentThread.join();
                System.out.println("Agent thread has finished execution.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Agent thread was interrupted.");
            }
            return agent;
        });
    }
}

