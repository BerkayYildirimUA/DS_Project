package nintendods.ds_project;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.database.FileControl;
import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.exeption.DuplicateFileException;
import nintendods.ds_project.exeption.DuplicateNodeException;
import nintendods.ds_project.exeption.NotEnoughMessageException;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.*;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.FileReader;
import nintendods.ds_project.utility.Generator;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Spring Boot application for managing a distributed system node's lifecycle excluding database auto-configuration.
 */
@EnableScheduling
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@Import(ClientNodeConfig.class)
public class Client {

    @Autowired
    ClientNode node; // Ahmad_merge: this or node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));

    @Autowired
    FileWatcherService fileWatcherService;
    public ClientNode getNode() {
        return node;
    }
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static NSAPIService API = NSAPIService.getAPI();

    @Autowired
    ApplicationContext context;
    private eNodeState nodeState;
    private int DISCOVERY_TIMEOUT = 8000; //In microseconds: Timeout for discovery

    private int NODE_NAME_LENGTH;      // Length of the random node name
    private int NODE_GLOBAL_PORT;      //Fixed port for node operations
    private int DISCOVERY_RETRIES;      // Maximum number of retries for discovery
    private int DISCOVERY_ADDITION_TIMEOUT;     //In microseconds
    private int LISTENER_BUFFER_SIZE;     // Buffer size for the listener service
    private String MULTICAST_ADDRESS;   // Multicast address for network communication
    private int MULTICAST_PORT;      // Port for multicast communication
    private UNAMObject nsObject;
    private boolean isRunning = true;

    private SyncAgent syncAgent = null;

    public SyncAgent getSyncAgent(){
        return syncAgent;
    }

    MulticastListenerService multicastListener = null;
    UnicastListenerService unicastListener = null;

    JsonConverter jsonConverter = new JsonConverter();

    @Value("${server.port}")
    private int apiPort;

    // File reader variables
    private final String path = System.getProperty("user.dir") + "/assets";
    private final FileDB fileDB = FileDBService.getFileDB();
    private final RestTemplate restTemplate = new RestTemplate();
    private FileTransceiverService fileTransceiver = new FileTransceiverService();


    public FileTransceiverService getFileTransceiver() {
        return fileTransceiver;
    }

    public void setFileTransceiver(FileTransceiverService fileTransceiver) {
        this.fileTransceiver = fileTransceiver;
    }

    //vars needed for testing
    private int testing;
    public int t_nextNodePort;
    public int t_prevNodePort;

    public static void main(String[] args) throws UnknownHostException, IOException {
        SpringApplication.run(Client.class, args);
    }

    public void setNsObject(UNAMObject nsObject) {
        this.nsObject = nsObject;
    }

    @PostConstruct
    private void init() throws UnknownHostException {
        NODE_NAME_LENGTH = ClientNodeConfig.getNodeNameLength();
        NODE_GLOBAL_PORT = ClientNodeConfig.getNodeGlobalPort();
        DISCOVERY_RETRIES = ClientNodeConfig.getDiscoveryRetries();
        DISCOVERY_ADDITION_TIMEOUT = ClientNodeConfig.getDiscoveryAdditionTimeout();
        LISTENER_BUFFER_SIZE = ClientNodeConfig.getListenerBufferSize();
        MULTICAST_ADDRESS = ClientNodeConfig.getMulticastAddress();
        MULTICAST_PORT = ClientNodeConfig.getMulticastPort();
        testing = ClientNodeConfig.getTESTING();

        if (testing == 1){
            t_prevNodePort = 0;
            t_nextNodePort = 0;
        }
    }

    @PreDestroy
    public void prepareForShutdown() throws InterruptedException {
        if ((nodeState != eNodeState.DISCOVERY) || (testing == 1)) {
            fileWatcherService.stopWatching();
            System.out.println("Preparing for shutdown...");
            ShutdownService shutdownService;

            if (testing == 1){
                shutdownService = new ShutdownService(node, nsObject, t_nextNodePort, t_prevNodePort);
            } else {
                shutdownService = new ShutdownService(node, nsObject);
            }

            shutdownService.emptyFileDatabase(fileTransceiver);
            shutdownService.updateNodesInSystem();
            System.out.println("Nodes prepared.");
            isRunning = false;
            System.out.println("PreDestroy done");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        try {
            new Thread(this::runNodeLifecycle).start();
        }
        catch (Exception ex){
            //TODO handle exception
        }
    }

    private void runNodeLifecycle() {
        DiscoveryService discoveryService = context.getBean(DiscoveryService.class);
        //MulticastSendService multicastSendService = context.getBean(MulticastSendService.class);
        //ListenerService listenerService = null; //context.getBean(ListenerService.class);
        //TransferService transferService = context.getBean(TransferService.class); // Assuming this service exists
        // Initialize the node with a random name and specific network settings
        // Create Node
        logger.info("New node with name: " + node.getName() + " And hash: " + node.getId());
        nodeState = eNodeState.DISCOVERY; // Initial state of the node
        boolean isRunning = true; // Controls the main loop
        int discoveryRetries = 0; // Counter for discovery attempts
        fileWatcherService.init();
        fileWatcherService.setFileChangeListener(this::onFileChanged);
        logger.info("Initialized FileWatcherService");
        logger.info("Node IP:" + node.getAddress().toString());

        while (isRunning) {

            // Finite state machine with eNodeState states
            switch (nodeState) {
                case DISCOVERY -> {
                    // Set Discovery on
                    if (discoveryRetries == DISCOVERY_RETRIES) {
                        // Max retries reached
                        logger.error("Max discovery retries reached");
                        nodeState = eNodeState.ERROR;
                        break;
                    }
                    DiscoveryService ds = new DiscoveryService(MULTICAST_ADDRESS, MULTICAST_PORT, DISCOVERY_TIMEOUT);
                    try {
                        logger.info("do discovery with node");
                        logger.info(node.toString());

                        ds.discover(node); // discover returns void
                        logger.info("Discovery done");

                    } catch (Exception e) {
                        discoveryRetries++;
                        if (discoveryRetries != DISCOVERY_RETRIES + 1) {
                            System.out.println("DISCOVERY:\t Retried discovery for the " + discoveryRetries + "(th) time");
                            logger.warn("Retry discovery for the" + discoveryRetries + "(th) time");
                        }
                        nodeState = eNodeState.DISCOVERY;

                        if (e instanceof DuplicateNodeException) {
                            //Create new node
                            node.setName(Generator.randomString(NODE_NAME_LENGTH)); // Ahmad_merge: this or node = new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH));
                            logger.info(node.toString());
                            logger.info("nodeName updated " + node.getName());
                        }
                        if (e instanceof NotEnoughMessageException) {
                            //Create other timeout
                            DISCOVERY_TIMEOUT += DISCOVERY_ADDITION_TIMEOUT;
                            logger.info("discoveryTimeout updated " + DISCOVERY_TIMEOUT);
                        }
                        break;
                    }

                    // //Discovery has succeeded so continue
                    // //get NSObject from discovery service
                    nsObject = ds.getNSObject(); //For later use
                    ApiUtil.setNsObject(nsObject);
                    // Configure the api object
                    API.setIp(nsObject.getNSAddress());
                    API.setPort(nsObject.getNSPort());

                    logger.info(node.toString());
                    logger.info("Successfully reply in " + discoveryRetries + " discoveries.");
                    nodeState = eNodeState.LISTENING; // Move to Listening state after successful discovery
                }
                case LISTENING -> {
                    //System.out.println("Entering Listening");
                    // Listen for multicast
                    if (multicastListener == null){
                        multicastListener = new MulticastListenerService(MULTICAST_ADDRESS, MULTICAST_PORT, LISTENER_BUFFER_SIZE);
                        multicastListener.initialize_multicast();
                    }

                    // Listen for unicast
                    if (unicastListener == null) {
                        unicastListener = new UnicastListenerService(3780);
                    }

                    // Listen for file transfers
                    try {
                        AFile file = null;
                        file = fileTransceiver.saveIncomingFile(node, path + "/replicated");
                        if(file != null){
                            System.out.println("LISTENING:\t get files\n" + file);
                        }
                    } catch (DuplicateFileException e) {
                        throw new RuntimeException(e);
                    }

                    // Update if needed
                    try {
                        multicastListener.listenAndUpdate(node);
                        unicastListener.listenAndUpdate(node);
                        nodeState = eNodeState.TRANSFER;

                    } catch (Exception e) {
                        e.printStackTrace();
                        nodeState = eNodeState.ERROR; // Move to Error state on exception
                    }
                    
                    if ( (node.getPrevNodeId() != -1 && node.getNextNodeId() != -1) && (node.getPrevNodeId() != node.getId() && node.getNextNodeId() != node.getId())){
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        nodeState = eNodeState.TRANSFER;
                    }
                    /*
                    if (node.getId() < node.getPrevNodeId())    {  // ---> gaat altijd een error geven vanaf je netwerk meer dan 2 nodes heeft
                        System.out.println("LISTENING:\t Client sleep");
                        try {
                            TimeUnit.SECONDS.sleep(15);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        nodeState = eNodeState.ERROR;
                    } else nodeState = eNodeState.TRANSFER;
                    */
                }
                case TRANSFER -> {
                    // TODO: Transfer data or handle other operations
                    List<File> files = FileReader.getFiles(path);
                    // System.out.println(files + "\n");

                    // Add files to DB
                    for (File file: files) fileDB.addOrUpdateFile(file, node);
                    // logger.info("TRANSFER:\t DB " + fileDB.getFiles());
                   // System.out.println("TRANSFER:\t node=" + node);
                   // System.out.println("TRANSFER:\t files read \n" + fileDB.getFiles());

                    // Transfer files
                    String transferIp, url;
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    ResponseEntity<String> response;
/*
                    for (AFile file: fileDB.getFiles()) {

                        // Get ip if the right node
                        url = "http://" + nsObject.getNSAddress() + ":8089/files/" + file.getName();
                        // logger.info("GET from: " + url);
                        System.out.println("GET from: " + url);
                        response = restTemplate.getForEntity(url, String.class);
                        transferIp = response.getBody();
                         System.out.println("Send file to " + transferIp);

                        System.out.println("TRANSFER:\t received=" + transferIp + "\n\t\t own=" + node.getAddress().getHostAddress());
                        if (("/"+node.getAddress().getHostAddress()).equals(transferIp)) {
                            // Node to send is self --> send to previous node
                            url = "http://" + nsObject.getNSAddress() + ":8089/node/" + node.getPrevNodeId();
                            // logger.info("GET from: " + url);
                            System.out.println("GET from: " + url);
                            response = restTemplate.getForEntity(url, String.class);
                            transferIp = response.getBody();
                            System.out.println("Can't send to self, redirect to " + transferIp);
                        }

                       // Send file to that node
                       logger.info(String.format("return of send %b ",fileTransceiver.sendFile(file, transferIp)));
                    }
*/
                   // System.out.println("TRANSFER:\t files added \n" + fileDB.getFiles());
                    nodeState = eNodeState.LISTENING; // Loop back to Listening for simplicity

                    //At the end of the transfer, we launch the sync agent towards the next node
                    if (syncAgent == null){
                        syncAgent = new SyncAgent(this.context);
                    }

                    //if (node.getAddress().toString().equals())
                    nodeState = eNodeState.LISTENING;
                }
                case SHUTDOWN -> {
                    System.out.println("SHUTDOWN:\t Start:" + Timestamp.from(Instant.now()));
                }
                case ERROR -> {
                    System.out.println("ERROR:\t Start:" + Timestamp.from(Instant.now()));

                    try {
                        ApiUtil.Client_POST_createFailureAgent(String.valueOf(node.getId()), String.valueOf(node.getPrevNodeId()));
                    } catch (IOException e){
                        logger.error(e.getMessage());
                    }

                    if (unicastListener != null)
                        unicastListener.stopListening();
                    if (multicastListener != null)
                        multicastListener.stopListening();

                    if (API.hasAddress()) {
                        System.out.println("ERROR:\t Client: Send error");
                        API.executeErrorDelete("/nodes/" + node.getId() + "/error");
                    }
                    /**
                     * When the node gets in the Error state, we'll access the
                     * NamingServer API to handle everything from here.
                     * call: {NSAddress}:{NSPort}/nodes/{id}/error
                     */
                    isRunning = false;
                }
                default -> {
                    // Same as error?
                }
            }
        }

        System.out.println("Main Done");
    }

    public void onFileChanged(File file) {
        fileDB.addOrUpdateFile(file, node);
        this.nodeState = eNodeState.TRANSFER;

        String targetDirectoryPath = path + File.separator + "local";
        moveFileToLocalDirectory(file, targetDirectoryPath);

    }

    private void moveFileToLocalDirectory(File file, String targetDirectoryPath) {
        try {
            // Create the target directory if it does not exist
            Path targetDirectory = Paths.get(targetDirectoryPath);
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }

            // Define the target file path
            Path targetFilePath = targetDirectory.resolve(file.getName());

            // Move the file to the target directory
            Files.move(file.toPath(), targetFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}