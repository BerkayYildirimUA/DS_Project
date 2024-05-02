package nintendods.ds_project.service;

import java.io.IOException;

public class UnicastListenService {
    private TCPServer client;

    public UnicastListenService() {
        Thread receiverThread = new Thread(() -> {
            try {
                client = new TCPServer();
                client.connect();
            } catch (IOException e) {
                System.out.println("UnicastListenService - Error:\tconstructor\n" + e + "\n");
                throw new RuntimeException(e);
            }
        });
        receiverThread.start();
    }

    public void stop() {
        try {
            client.stop();
        } catch (IOException e) {
            System.out.println("UnicastListenService - Error:\tstop\n" + e + "\n");
            throw new RuntimeException(e);
        }
    }

}
