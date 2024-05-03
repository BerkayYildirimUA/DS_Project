package nintendods.ds_project.service;

import nintendods.ds_project.database.NodeDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TreeMap;

public class TCPService {

    private static TCPService tcpService = null;

    public static TCPService getTcpService() {
        if(tcpService == null)
            tcpService = new TCPService();
        return tcpService;
    }

    private final Logger logger = LoggerFactory.getLogger(TCPService.class);
    private final TreeMap<Integer, TCPClient> tcpClients = new TreeMap<>();
    NodeDB nodeDB = NodeDBService.getNodeDB();

    private void printAndLog(String message) {
        logger.info(message);
        System.out.println(message);
    }

    public Thread createTCPThread(int neighborId, int deletedId, int replacementId) {
        return new Thread(() -> {
            TCPClient client = new TCPClient();
            tcpClients.put(neighborId, client);
            // Send delete to neighboring nodes
            try {
                printAndLog("TCPService: Start connection with "+ neighborId +" nodes");
                client.connect(nodeDB.getIpFromId(neighborId), deletedId + "->" + replacementId);
            } catch (IOException e) {
                printAndLog("TCPService: Failed connection with "+ neighborId +" nodes");
                tcpClients.remove(neighborId);
                throw new RuntimeException(e);
            }

            // Delete node from database after the neighbors have
            try {
                printAndLog("TCPService: Delete node");
                nodeDB.deleteNode(deletedId);
            } catch (Exception e) {
                printAndLog(e.toString());
            }
        });
    }

    public void stopClient(int id) {
        TCPClient client = tcpClients.get(id);
        try {
            if (client != null) client.stop();
            tcpClients.remove(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
