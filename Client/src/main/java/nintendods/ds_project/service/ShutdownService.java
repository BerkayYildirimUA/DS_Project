package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.utility.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ShutdownService {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownService.class);
    public int t_nextNodePort = 0;
    public int t_prevNodePort = 0;
    ClientNode node;
    UNAMObject nsObject;
    @Value("${server.port}")
    private int apiPort;


    public ShutdownService(ClientNode node, UNAMObject nsObject, int t_nextNodePort, int t_prevNodePort) {
        this.node = node;
        this.nsObject = nsObject;
        this.t_nextNodePort = t_nextNodePort;
        this.t_prevNodePort = t_prevNodePort;
    }

    public ShutdownService(ClientNode node, UNAMObject nsObject) {
        this.node = node;
        this.nsObject = nsObject;
    }

    public void emptyFileDatabase() {
        FileDB fileDB = FileDBService.getFileDB();

        List<AFile> files = fileDB.getFiles();

        for (AFile file : files) {
            FileTransceiverService fileTransceiverService = new FileTransceiverService();
            String prevNodeIP = ApiUtil.NameServer_GET_NodeIPfromID(Integer.toString(node.getPrevNodeId()));
            fileTransceiverService.sendFile(file, prevNodeIP);
        }
    }


    public void updateNodesInSystem() {
        RestTemplate restTemplate = new RestTemplate();

        //if you are alone in the network then skip
        if (node.getId() != node.getPrevNodeId()) {
            int nextNodePort;
            int prevNodePort;

            String prevNodeIP;
            String nextNodeIP;

            //multiple springboot appliactions can't be run on the same port on the same machine. So this is made, so I can change the Port numbers dynamicly during testing.
            if (t_nextNodePort != 0 && t_prevNodePort != 0) {
                nextNodePort = t_nextNodePort;
                prevNodePort = t_prevNodePort;

                prevNodeIP = "/127.0.0.1";
                nextNodeIP = "/127.0.0.1";
            } else {
                nextNodePort = apiPort;
                prevNodePort = apiPort;


                String urlGetNextNodeIP = "http://" + nsObject.getNSAddress() + ":8089/node/" + node.getNextNodeId();
                logger.info("GET from: " + urlGetNextNodeIP);
                ResponseEntity<String> getNextNodeIDResponse = restTemplate.getForEntity(urlGetNextNodeIP, String.class);
                nextNodeIP = getNextNodeIDResponse.getBody();

                String urlGetPrevNodeIP = "http://" + nsObject.getNSAddress() + ":8089/node/" + node.getPrevNodeId();
                logger.info("GET from: " + urlGetPrevNodeIP);
                ResponseEntity<String> getPrevNodeIDResponse = restTemplate.getForEntity(urlGetPrevNodeIP, String.class);
                prevNodeIP = getPrevNodeIDResponse.getBody();

            }

            // in PREV node I need to change their NEXT node to my NEXT node
            // http heeft 1 '/' in de plaats van 2 want the IP's strings starten met '/' en ik will dit niet uit filtreren.
            String UrlForPrevNode = "http:/" + prevNodeIP + ":" + prevNodePort + "/api/Management/nextNodeID/?ID=" + node.getNextNodeId();
            logger.info("PUT to: " + UrlForPrevNode);
            restTemplate.put(UrlForPrevNode, String.class);

            // in NEXT node I need to change their PREV node to my PREV node
            String urlForNextNode = "http:/" + nextNodeIP + ":" + nextNodePort + "/api/Management/prevNodeID/?ID=" + node.getPrevNodeId();
            logger.info("PUT to: " + urlForNextNode);
            restTemplate.put(urlForNextNode, String.class);
        }
        //delete yourself from nameserver
        String urlDeleteNode = "http://" + nsObject.getNSAddress() + ":8089/nodes/" + node.getId();
        logger.info("DELETE from: " + urlDeleteNode);
        restTemplate.delete(urlDeleteNode);
    }
}
