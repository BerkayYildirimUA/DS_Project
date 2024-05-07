package nintendods.ds_project.service;

import nintendods.ds_project.model.ClientNode;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UnicastListenerService {
    private TCPServer server;

    public UnicastListenerService(@Value("${tcp.unicast.port}") int port) {
        this.server = new TCPServer(port);
        Thread receiverThread = new Thread(() -> {
            try {
                System.out.println(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
            try {
                server.connect();
            } catch (IOException e) {
                System.out.println("UnicastListenService - Error:\tconnect failed\n");
                //throw new RuntimeException(e);
            }
            try {
                server.listen();
            } catch (IOException e) {
                System.out.println("UnicastListenService - Error:\tlisten failed\n");
                //throw new RuntimeException(e);
            }
        });
        receiverThread.start();
    }

    public void listenAndUpdate(ClientNode node) {
        Pair<Integer, Integer> newIdConfig = server.decryptMessage();
        if (newIdConfig != null) {
            if (node.getPrevNodeId() == newIdConfig.a && node.getNextNodeId() == newIdConfig.a) {
                // 2 Nodes in the topology of which one is in error state
                node.setPrevNodeId(-1);
                node.setNextNodeId(-1);
            } else if (node.getPrevNodeId() == newIdConfig.a) {
                // Previous node is in error state
                node.setPrevNodeId(newIdConfig.b);
            } else {
                // Next node is in error state
                node.setNextNodeId(newIdConfig.b);
            }
            if (NSAPIService.getAPI().hasAddress())
                NSAPIService.getAPI().executeErrorPatch("/nodes/" + node.getId() + "/error");
        }
    }

    public void stopListening() {
        try {
            server.stop();
        } catch (IOException e) {
            // System.out.println("UnicastListenService - Error:\tstop\n" + e + "\n");
            System.out.println("Socket and data-stream already closed");
            // throw new RuntimeException(e);
        }
    }

}
