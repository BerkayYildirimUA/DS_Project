package nintendods.ds_project.service;

import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.database.NodeDB;
import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.NodeModel;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MulticastHandler {
    private static final String MULTICAST_ADDRESS = "223.0.0.1000";
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 20;

    public MulticastHandler() {
        BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>();

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
            e.printStackTrace();
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
                ABaseNode node = new NodeModel(receivedObject);
                //Check database if node exist
                if (nodeDB.exists(node)){
                    //Add node to database
                    nodeDB.addNode(node);
                }

                // Compose response to node
                // amount of nodes present in ring
                int amountNodes = nodeDB.

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (NameServerFullExeption e) {
                throw new RuntimeException(e);
            }
        }
    }
}
