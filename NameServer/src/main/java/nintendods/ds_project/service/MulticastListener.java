package nintendods.ds_project.service;

import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.utility.JsonConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MulticastListener extends Thread {
    private MulticastSocket socket = null;
    private byte[] buf = new byte[256];
    // https://www.baeldung.com/java-queue-linkedblocking-concurrentlinked#linkedblockingqueue
    // https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/LinkedBlockingQueue.html
    BlockingQueue<MNObject> incommingQueue = new LinkedBlockingQueue<>(20);

    public void run() {
        DatagramPacket packet = null;
        InetAddress group = null;
        JsonConverter jsonConverter = new JsonConverter();

        try {
            socket = new MulticastSocket(12345);
            group = InetAddress.getByName("224.0.0.100");
            socket.joinGroup(group);
            System.out.println("Listening for multicasts");
            packet = new DatagramPacket(buf, buf.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                socket.receive(packet);
                String receivedJson = new String(packet.getData(), 0, packet.getLength());
                System.out.println(receivedJson);

                MNObject receivedObject = (MNObject) jsonConverter.toObject(receivedJson, MNObject.class);

                //Only add if the message is not yet in the queue.
                // UDP message can be sent more than once.
                if (incommingQueue.stream().noneMatch(c -> (c.toString().equals(receivedObject.toString()))))
                    incommingQueue.add(receivedObject);

            } catch (IOException e) {
                break;
            }
        }

        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the Multicast message from the queue
     * Is blocking when the queue is empty.
     * @return a {@link MNObject}
     */
    public MNObject getMNMessage() {
        try {
            return this.incommingQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}