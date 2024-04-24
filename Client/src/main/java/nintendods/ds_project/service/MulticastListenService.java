package nintendods.ds_project.service;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.eMessageTypes;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MulticastListenService {
    private static final int BUFFER_SIZE = 1024;
    private static BlockingQueue<MNObject> multicastQueue;
    private final JsonConverter jsonConverter = new JsonConverter();

    String multicastAddress;
    int multicastPort;
    int multicastBufferCapacity;

    /**
     * A multicast service that wil listen to multicast messages and parse these to MNObject. 
     * @param multicastAddress The address where we listen onto
     * @param multicastPort The UDP port 
     * @param multicastBufferCapacity The maximum messages we can store in the queue
     * @throws RuntimeException
     */
    public MulticastListenService(@Value("${udp.multicast.address}") String multicastAddress,
                                  @Value("${udp.multicast.port}") int multicastPort,
                                  @Value("${udp.multicast.buffer-capacity}") int multicastBufferCapacity) throws RuntimeException {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.multicastBufferCapacity = multicastBufferCapacity;
    }

//    public MulticastListenService(String multicastAddress,
//                                  int multicastPort,
//                                  int multicastBufferCapacity) throws RuntimeException {
//        this.multicastAddress = multicastAddress;
//        this.multicastPort = multicastPort;
//        this.multicastBufferCapacity = multicastBufferCapacity;
//    }

    public void initialize() {
        BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>(multicastBufferCapacity);
        multicastQueue = new LinkedBlockingQueue<>(multicastBufferCapacity);
        System.out.println("MulticastService - Setup multicast listener");
        // Start the receiver thread
        Thread receiverThread = new Thread(() -> receivePackets(packetQueue, multicastAddress, multicastPort));
        receiverThread.start();

        // Start the processor thread
        Thread processorThread = new Thread(() -> processPackets(packetQueue));
        processorThread.start();

        System.out.println("MulticastService - Started multicast listener");
    }

    private void receivePackets(BlockingQueue<String> packetQueue, String multicastAddress, int multicastPort) {
        try (MulticastSocket multicastSocket = new MulticastSocket(multicastPort)) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
                System.out.println("MulticastService - Received a multicast");
                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(message);
                //Only add if the message is not yet in the queue.
                // UDP message can be sent more than once.
                if (packetQueue.stream().noneMatch(c -> (c.equals(message)))) packetQueue.offer(message); // Add packet to the queue
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
                System.out.println("MulticastService - Get packet from queue");

                //Check if multicast is from a new node
                if (packet.contains(eMessageTypes.MulticastNode.name())) {
                    //cast to it
                    MNObject mnObject = (MNObject) jsonConverter.toObject(packet, MNObject.class);
                    try { multicastQueue.put(mnObject); } 
                    catch (InterruptedException e) { throw new RuntimeException(e);
                    }
                }
            } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
    }

    public void sendReply(UNAMNObject reply, ABaseNode toNode) throws IOException {
        long id = System.currentTimeMillis();
        reply.setMessageId(id);

        UDPClient client = new UDPClient(toNode.getAddress(),toNode.getPort(), BUFFER_SIZE);

        //Send out 2 times, the receiver must filter out packets with the same ID.
        client.SendMessage(jsonConverter.toJson(reply));
        client.SendMessage(jsonConverter.toJson(reply));
        System.out.println("MulticastService - Send out 1 packs of UNAMObjects on port: " + toNode.getPort());
        client.close();
    }

    public MNObject getMessage() throws NullPointerException{
        return multicastQueue.poll();
    }
}
