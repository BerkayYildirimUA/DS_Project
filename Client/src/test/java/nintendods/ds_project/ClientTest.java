package nintendods.ds_project;

import nintendods.ds_project.model.ClientNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {



    public Map<Integer, SimpleNode> getCurenntNodes(List<CompletableFuture<ConfigurableApplicationContext>> futures) throws ExecutionException, InterruptedException {
        Map<Integer, SimpleNode> nodes = new HashMap<>();

        //loop to read the data of all the nodes all the nodes
        for (CompletableFuture<ConfigurableApplicationContext> future : futures) {
            ClientNode node = future.get().getBean(Client.class).getNode();
            SimpleNode data = new SimpleNode(future, node);
            nodes.put(node.getId(), data);
        }

        //loop to fill in next and prev node ports
        System.out.println(nodes);

        for (Integer id : nodes.keySet()) {
            SimpleNode node = nodes.get(id);
            System.out.println(node.toString());
            System.out.println(id);
            node.prevPort = nodes.get(node.prevID).myPort;
            node.nextPort = nodes.get(node.nextID).myPort;
        }

        return nodes;
    }

    @Test
    void contextLoads() {
    }

    @Test
        //if it fails, try running it a few more times. First time I'm working with threads this complex so sometimes things don't proparly work because of the way the threads interact, not because of the project itself. It seems pretty stable now though.
    void shutdownTest() throws InterruptedException, ExecutionException {
        System.out.println("START NAMESERVER FOR THIS TEST BY HAND ");

        //start multiple nodes
        int numberOfInstances = 4;

        List<CompletableFuture<ConfigurableApplicationContext>> futureContexts = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(numberOfInstances);

        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(numberOfInstances, Runtime.getRuntime().availableProcessors())); //use a fix number of threads. Either the least amount needed for the job, or deping on how many your CPU can handel.

        IntStream.range(0, numberOfInstances).forEach(i -> { //basicly a "for" loop, but I can't use "i" in the thread if I did that instead


            CompletableFuture<ConfigurableApplicationContext> future = CompletableFuture.supplyAsync(() -> {
                String[] arg = {"--server.port=807" + i, "--TESTING=1"};
                String threadName = "Node-807" + i;
                Thread.currentThread().setName(threadName);
                Thread.currentThread().setPriority(7);
                ConfigurableApplicationContext context = SpringApplication.run(Client.class, arg);
                System.out.println(threadName + " started.");

                Runtime.getRuntime().addShutdownHook(new Thread(context::close));

                latch.countDown();
                return context;
            }, executorService);

            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            futureContexts.add(future);
        });

        System.out.println("waiting for latches");
        latch.await();

        while (!futureContexts.isEmpty()) {

            Map<Integer, SimpleNode> nodesBeforeDestruction = getCurenntNodes(futureContexts);

            //if these fail then something went wrong in the setup
            for (SimpleNode controlNodes : nodesBeforeDestruction.values()) {
                // if these fail it's because discovry went wrong because if the threads
                assertEquals(nodesBeforeDestruction.get(controlNodes.nextID).prevID, controlNodes.myID);
                assertEquals(nodesBeforeDestruction.get(controlNodes.prevID).nextID, controlNodes.myID);

                //check if nodes that will be detroyed exists, same thing as abode, if it fails it's probably because of the threads
                RestTemplate restTemplate = new RestTemplate();
                String urlGetMyExistence = "http://127.0.0.1:8089/node/" + controlNodes.myID;
                ResponseEntity<String> getMyNodeIDResponse = restTemplate.getForEntity(urlGetMyExistence, String.class);
                assertSame(getMyNodeIDResponse.getStatusCode(), HttpStatus.OK);
            }

            // Grab a random node to destroy
            Random random = new Random();
            SimpleNode[] values = nodesBeforeDestruction.values().toArray(new SimpleNode[0]);
            SimpleNode nodeThatWillBeDestroyed = values[random.nextInt(values.length)];

            // Making him ready for deletion
            Client app = nodeThatWillBeDestroyed.future.get().getBean(Client.class);
            app.t_nextNodePort = nodeThatWillBeDestroyed.nextPort;
            app.t_prevNodePort = nodeThatWillBeDestroyed.prevPort;

            // Close the context and wait for it to complete
            nodeThatWillBeDestroyed.future.thenAccept(ConfigurableApplicationContext::close).join();

            // Remove the future from the list
            futureContexts.remove(nodeThatWillBeDestroyed.future);
            TimeUnit.SECONDS.sleep(5); // time buffer for safety

            // Check the state after destruction
            if (!futureContexts.isEmpty()) {
                Map<Integer, SimpleNode> nodesPostDestruction = getCurenntNodes(futureContexts);
                assertEquals(nodesPostDestruction.get(nodeThatWillBeDestroyed.prevID).nextID, nodeThatWillBeDestroyed.nextID);
                assertEquals(nodesPostDestruction.get(nodeThatWillBeDestroyed.nextID).prevID, nodeThatWillBeDestroyed.prevID);
            }

            try {
                RestTemplate restTemplate = new RestTemplate();
                String urlGetMyExistence = "http://127.0.0.1:8089/node/" + nodeThatWillBeDestroyed.myID;
                restTemplate.getForEntity(urlGetMyExistence, String.class);
                fail("Expected HttpClientErrorException.NotFound");
            } catch (HttpClientErrorException.NotFound e) {
                assertTrue(e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
            }
        }
    }





    //basically gewoon een struct. Maakt het makelijker om de data op te halen
    public class SimpleNode {
        public int myPort;
        public int myID;

        public int nextPort;
        public int nextID;

        public int prevPort;
        public int prevID;

        CompletableFuture<ConfigurableApplicationContext> future;

        public SimpleNode(CompletableFuture<ConfigurableApplicationContext> future, ClientNode node) throws ExecutionException, InterruptedException {
            this.myPort = Integer.parseInt(Objects.requireNonNull(future.get().getEnvironment().getProperty("local.server.port")));
            this.future = future;

            this.myID = node.getId();
            this.nextID = node.getNextNodeId();
            this.prevID = node.getPrevNodeId();
        }

        @Override
        public String toString() {
            return "SimpleNode{" +
                    "myPort=" + myPort +
                    ", myID=" + myID +
                    ", nextPort=" + nextPort +
                    ", nextID=" + nextID +
                    ", prevPort=" + prevPort +
                    ", prevID=" + prevID +
                    '}';
        }
    }


}
