package nintendods.ds_project;

import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.model.ClientNode;
import org.apache.el.parser.SimpleNode;
import org.aspectj.weaver.ast.Call;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DsProjectApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void shutdownTest() throws InterruptedException, ExecutionException {
        System.out.println("START NAMESERVER FOR THIS TEST BY HAND ");

        //start multiple nodes
        int numberOfInstances = 4;

        List<CompletableFuture<ConfigurableApplicationContext>> futureContexts = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(numberOfInstances);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfInstances);

        IntStream.range(0, numberOfInstances).forEach(i -> {
            CompletableFuture<ConfigurableApplicationContext> future = CompletableFuture.supplyAsync(() -> {
                String[] arg = {"--server.port=807" + i, "--TESTING=1"};
                String threadName = "Node-807" + i;
                Thread.currentThread().setName(threadName);

                ConfigurableApplicationContext context = SpringApplication.run(DsProjectApplication.class, arg);
                System.out.println(threadName + " started.");

                Runtime.getRuntime().addShutdownHook(new Thread(context::close));

                latch.countDown();
                return context;
            }, executorService);

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            futureContexts.add(future);
        });


        System.out.println("waiting for latches");
        latch.await();

        Map<Integer, SimpleNode> nodesBeforeDestruction = getCurenntNodes(futureContexts);

        //grab a random node to destroy
        Random random = new Random();
        SimpleNode[] values = nodesBeforeDestruction.values().toArray(new SimpleNode[0]);
        SimpleNode nodeThatWillBeDestroyed = values[random.nextInt(values.length)];

        //ckeck of hij bestaat
        assertEquals(nodesBeforeDestruction.get(nodeThatWillBeDestroyed.nextID).prevID, nodeThatWillBeDestroyed.myID);
        assertEquals(nodesBeforeDestruction.get(nodeThatWillBeDestroyed.prevID).nextID, nodeThatWillBeDestroyed.myID);
        RestTemplate restTemplate = new RestTemplate();
        String urlGetPrevNodeID = "http://127.0.0.1:8089/node/" + nodeThatWillBeDestroyed.myID;
        ResponseEntity<String> getMyNodeIDResponse = restTemplate.getForEntity(urlGetPrevNodeID, String.class);
        assertSame(getMyNodeIDResponse.getStatusCode(), HttpStatus.OK);

        //making him ready for deletion
        DsProjectApplication app = nodeThatWillBeDestroyed.future.get().getBean(DsProjectApplication.class);
        app.t_nextNodePort = nodeThatWillBeDestroyed.nextPort;
        app.t_prevNodePort = nodeThatWillBeDestroyed.prevPort;


        nodeThatWillBeDestroyed.future.thenAccept(ConfigurableApplicationContext::close)
                .join();

        futureContexts.remove(nodeThatWillBeDestroyed.future);
        TimeUnit.SECONDS.sleep(5);


        Map<Integer, SimpleNode> nodesPostDestruction = getCurenntNodes(futureContexts);

        assertEquals(nodesPostDestruction.get(nodeThatWillBeDestroyed.prevID).nextID, nodeThatWillBeDestroyed.nextID);
        assertEquals(nodesPostDestruction.get(nodeThatWillBeDestroyed.nextID).prevID, nodeThatWillBeDestroyed.prevID);

        try {
            restTemplate.getForEntity(urlGetPrevNodeID, String.class);
            fail("Expected HttpClientErrorException.NotFound");

        } catch (HttpClientErrorException.NotFound e) {
            assertTrue(e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
        }
    }

    public Map<Integer, SimpleNode> getCurenntNodes(List<CompletableFuture<ConfigurableApplicationContext>> futures) throws ExecutionException, InterruptedException {
        Map<Integer, SimpleNode> nodes = new HashMap<>();

        //loop to read the data of all the nodes all the nodes
        for (CompletableFuture<ConfigurableApplicationContext> future : futures){
            ClientNode node = future.get().getBean(DsProjectApplication.class).getNode();
            SimpleNode data = new SimpleNode(future, node);
            nodes.put(node.getId(), data);
        }

        //loop to fill in next and prev node ports
        System.out.println(nodes);
        for (Integer id: nodes.keySet()){
            SimpleNode node = nodes.get(id);
            System.out.println(node.toString());
            System.out.println(id);
            node.prevPort = nodes.get(node.prevID).myPort;
            node.nextPort = nodes.get(node.nextID).myPort;
        }

        return nodes;
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
