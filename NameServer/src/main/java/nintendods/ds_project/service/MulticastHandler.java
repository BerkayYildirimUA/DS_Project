package nintendods.ds_project.service;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MulticastHandler {
    private static final String MULTICAST_ADDRESS = "224.0.0.100";
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 256;

    /**
     * Handler keeps running and listening for multicasts from joining nodes.
     * @throws RuntimeException
     */
    public MulticastHandler() throws RuntimeException {
        BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>(20);

        // Start the receiver thread
        Thread receiverThread = new Thread(() -> receivePackets(packetQueue));
        receiverThread.start();

        // Start the processor thread
        Thread processorThread = new Thread(() -> processPackets(packetQueue));
        processorThread.start();
    }

    private static void receivePackets(BlockingQueue<String> packetQueue) {
        try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
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

    private static void processPackets(BlockingQueue<String> packetQueue) {
        JsonConverter jsonConverter = new JsonConverter();
        NodeDB nodeDB = NodeDBService.getNodeDB();
        while (true) {
            try {
                String packet = packetQueue.take(); // Retrieve packet from the queue
                // Process the packet (example: print it)
                System.out.println("Received packet: " + packet);
                MNObject receivedObject = (MNObject) jsonConverter.toObject(packet, MNObject.class);

                //Create the node and hash ID (cast to ClientNode to get the ID)
                ABaseNode node = new ClientNode(receivedObject);

                //Check database if node exist
                if (!nodeDB.exists(node)) {
                    // Compose response to node based on UNAMObject
                    // amount of nodes present in ring
                    int amountNodes = nodeDB.getSize();

                    //Add to database
                    //TODO: remove debug
                    nodeDB.addNode(node);
                    System.out.println("Node " + node.getName() +" added");

                    // Send out the multicast message over UDP with the timestamp as ID.
                    long messageId = System.currentTimeMillis();
                    UNAMObject unicastMessage = new UNAMObject(messageId, eMessageTypes.UnicastNamingServerToNode, amountNodes);

                    //Setup the UDP sender and send out.
                    UDPClient client = new UDPClient(node.getAddress(),node.getPort(), 256);
                    client.SendMessage(jsonConverter.toJson(unicastMessage));
                    client.SendMessage(jsonConverter.toJson(unicastMessage));
                    System.out.println("Send out 2 packs of UNAMObjects ");
                }
                else System.out.println("Node already exists");

            } catch (InterruptedException | IOException | NameServerFullExeption e) {
                throw new RuntimeException(e);
            }
        }
    }
}
