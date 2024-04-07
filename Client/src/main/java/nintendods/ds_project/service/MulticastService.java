package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.model.message.eMessageTypes;
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
public class MulticastService {
    private static final String MULTICAST_ADDRESS = "224.0.0.100";
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 256;
    private static BlockingQueue<MNObject> multicastQueue;
    private final JsonConverter jsonConverter = new JsonConverter();

    /**
     * Handler keeps running and listening for multicasts from joining nodes.
     * @throws RuntimeException
     */
    public MulticastService() throws RuntimeException {
        BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>(20);
        multicastQueue = new LinkedBlockingQueue<>(20);
        System.out.println("Setup multicast listener");
        // Start the receiver thread
        Thread receiverThread = new Thread(() -> receivePackets(packetQueue));
        receiverThread.start();

        // Start the processor thread
        Thread processorThread = new Thread(() -> processPackets(packetQueue));
        processorThread.start();
    }

    private void receivePackets(BlockingQueue<String> packetQueue) {
        try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
                System.out.println("Received a multicast");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
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

        while (true) {
            String packet = null;
            try {
                packet = packetQueue.take();
                System.out.println("Get packet from queue");

                //Check if multicast is from a new node
                if (packet.contains(eMessageTypes.MulticastNode.name())) {
                    //cast to it
                    MNObject mnObject = (MNObject) jsonConverter.toObject(packet, MNObject.class);
                    try {
                        multicastQueue.put(mnObject);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendReply(UNAMNObject reply, ABaseNode toNode) throws IOException {
        //Setup udp unicast
        long id = System.currentTimeMillis();
        reply.setMessageId(id);
        System.out.println("Send out files from node to node");
        System.out.println(reply);
        System.out.println(toNode);
        UDPClient client = new UDPClient(toNode.getAddress(),toNode.getPort(), 1024);

        try {
            long randomDelay = (long) (Math.random() * 200); // Generate a random delay between 0 and 200 nanoseconds
            Thread.sleep(0, (int) randomDelay); // Sleep for the random delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread sleep interrupted");
        }

        client.SendMessage(jsonConverter.toJson(reply));
        //client.SendMessage(jsonConverter.toJson(reply));
        System.out.println("Send out 1 packs of UNAMObjects on port: " + toNode.getPort());
        client.close();
    }

    public MNObject getMessage() throws NullPointerException{
        return multicastQueue.poll();
    }
}
