package nintendods.ds_project.service;

import jakarta.annotation.PostConstruct;
import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MulticastListenService {
    private static final int BUFFER_SIZE = 256; // Buffer size for incoming datagram packets
    private static BlockingQueue<MNObject> multicastQueue; // Queue to store processed multicast messages
    private final JsonConverter jsonConverter = new JsonConverter(); // JSON converter for parsing JSON to objects

    String multicastAddress;
    int multicastPort;
    int multicastBufferCapacity;

    /**
     * A multicast service that wil listen to multicast messages and parse these to MNObject. 
     * @param multicastAddress The address where we listen onto
     * @param multicastPort The UDP port to use for multicast listening
     * @param multicastBufferCapacity The maximum messages we can store in the queue
     * @throws RuntimeException if there is a problem setting up the multicast socket
     */
    public MulticastListenService(@Value("${udp.multicast.address}") String multicastAddress,
                                  @Value("${udp.multicast.port}") int multicastPort,
                                  @Value("${udp.multicast.buffer-capacity}") int multicastBufferCapacity){
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.multicastBufferCapacity = multicastBufferCapacity;
    }

    public void initialize() {
            BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>(multicastBufferCapacity);
            multicastQueue = new LinkedBlockingQueue<>(multicastBufferCapacity);
            System.out.println("MulticastService - Setup multicast listener");

            // Start the receiver thread
            // Receiver thread to handle incoming multicast packets
            Thread receiverThread = new Thread(() -> receivePackets(packetQueue, multicastAddress, multicastPort));
            receiverThread.start();

            // Start the processor thread
            // Processor thread to parse JSON messages into MNObject instances and add them to the queue
            Thread processorThread = new Thread(() -> processPackets(packetQueue));
            processorThread.start();

            System.out.println("MulticastService - Started multicast listener");
    }

    /**
     * Receives packets from a multicast group and adds them to a processing queue.
     * @param packetQueue Queue to store incoming packet data for processing
     * @param multicastAddress Multicast group IP address
     * @param multicastPort Port used for the multicast group
     */
    private void receivePackets(BlockingQueue<String> packetQueue, String multicastAddress, int multicastPort) {
        try (MulticastSocket multicastSocket = new MulticastSocket(multicastPort)) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            multicastSocket.joinGroup(group); // Join the multicast group to receive packets

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet); // Block until a packet is received
                System.out.println("MulticastService - Received a multicast");
                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(message);
                //Only add if the message is not yet in the queue.
                // UDP message can be sent more than once.
                if (packetQueue.stream().noneMatch(c -> (c.equals(message))))
                    packetQueue.offer(message); // Add unique packet to the queue
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // Rethrow as unchecked exception
        }
    }

    /**
     * Processes packets from the queue, converting JSON strings to MNObject instances.
     * @param packetQueue Queue containing raw packet data as strings
     */
    private void processPackets(BlockingQueue<String> packetQueue) {
        while (true) {
            String packet = null;
            try {
                packet = packetQueue.take(); // Block until a packet is available
                System.out.println("MulticastService - Get packet from queue");

                //Check if multicast is from a new node
                if (packet.contains(eMessageTypes.MulticastNode.name())) {
                    //cast to it
                    MNObject mnObject = (MNObject) jsonConverter.toObject(packet, MNObject.class);
                    try {
                        multicastQueue.put(mnObject); // Add the MNObject to the multicast message queue
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e); // Rethrow as unchecked exception
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sends a reply message via UDP to a specific node.
     * @param reply The reply message to send
     * @param toNode The destination node information
     * @throws IOException if there is a problem sending the message
     */
    public void sendReply(UNAMNObject reply, ABaseNode toNode) throws IOException {
        //Setup udp unicast
        long id = System.currentTimeMillis(); // Unique ID for the reply message based on timestamp
        reply.setMessageId(id);
        //System.out.println("MulticastService - Send out files from node to node");
        //System.out.println("\t"+reply);
        //System.out.println("\t"+toNode);
        UDPClient client = new UDPClient(toNode.getAddress(),toNode.getPort(), 1024);

        //Send out 2 times, the receiver must filter out packets with the same ID: Serialize reply and send it twice (handling UDP unreliability)
        client.SendMessage(jsonConverter.toJson(reply));
        client.SendMessage(jsonConverter.toJson(reply));
        System.out.println("MulticastService - Send out 1 packs of UNAMObjects on port: " + toNode.getPort());
        client.close(); // Close the UDP client after sending the messages
    }

    /**
     * Retrieves a multicast message from the queue.
     * @return MNObject from the queue, or null if the queue is empty
     */
    public MNObject getMessage() throws NullPointerException{
        return multicastQueue.poll(); // Return the next available message or null if none available
    }
}
