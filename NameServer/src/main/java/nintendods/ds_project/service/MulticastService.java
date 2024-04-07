package nintendods.ds_project.service;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.utility.JsonConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Component
public class MulticastService {

    final Logger logger = LoggerFactory.getLogger(MulticastService.class);


    private static final String MULTICAST_ADDRESS = "224.0.0.100";
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 256;

    /**
     * Handler keeps running and listening for multicasts from joining nodes.
     * @throws RuntimeException
     */
    public MulticastService() throws RuntimeException {
        logger.info("Starting up MulticastService");
        BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>(20);

        // Start the receiver thread
        Thread receiverThread = new Thread(() -> receivePackets(packetQueue));
        receiverThread.start();

        // Start the processor thread
        Thread processorThread = new Thread(() -> processPackets(packetQueue));
        processorThread.start();
    }

    private void receivePackets(BlockingQueue<String> packetQueue) {
        try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {
            logger.info("Start thread receivePackets");
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
                logger.info("Received a packet in receivePackets thread");
                String message = new String(packet.getData(), 0, packet.getLength());

                //Only add if the message is not yet in the queue.
                // UDP message can be sent more than once.
                if (packetQueue.stream().noneMatch(c -> (c.equals(message))))
                    packetQueue.offer(message); // Add packet to the queue
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processPackets(BlockingQueue<String> packetQueue) {
        logger.info("Start thread processPackets");
        JsonConverter jsonConverter = new JsonConverter();
        NodeDB nodeDB = NodeDBService.getNodeDB();
        while (true) {
            try {
                String packet = packetQueue.take(); // Retrieve packet from the queue
                // Process the packet (example: print it)
                logger.info("process a packet in processPackets thread");
                //System.out.println("Received packet: " + packet);
                MNObject receivedObject = (MNObject) jsonConverter.toObject(packet, MNObject.class);
                logger.info("packet from "+ receivedObject.getName());
                //Create the node and hash ID (cast to ClientNode to get the ID)
                ABaseNode node = new ClientNode(receivedObject);

                // Compose response to node based on UNAMObject
                // amount of nodes present in ring
                int amountNodes = nodeDB.getSize();

                //Check database if node exist
                if (!nodeDB.exists(node)) {
                    logger.info("Adding node " + node.getName() + " to DB");

                    //Add to database
                    nodeDB.addNode(node);
                }
                else{
                    logger.info("Node " + node.getName() + " already exists in DB");
                    amountNodes--;
                }

                sendReply(node, amountNodes);

            } catch (InterruptedException | IOException | NameServerFullExeption e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void sendReply(ABaseNode node, int amount) throws IOException {
        JsonConverter jsonConverter = new JsonConverter();
        logger.info("Sending unicast to " + node.getName() + " on address: " + node.getAddress().toString() + ", port: " + node.getPort());
        // Send out the multicast message over UDP with the timestamp as ID.
        long messageId = System.currentTimeMillis();
        UNAMObject unicastMessage = new UNAMObject(messageId, eMessageTypes.UnicastNamingServerToNode, amount);

        //Setup the UDP sender and send out.
        UDPClient client = new UDPClient(node.getAddress(),node.getPort(), 256);

        //Send out 2 times, the receiver must filter out packets with the same ID.
        client.SendMessage(jsonConverter.toJson(unicastMessage));
        client.SendMessage(jsonConverter.toJson(unicastMessage));
        client.close();
    }
}