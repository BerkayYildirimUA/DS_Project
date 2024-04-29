package nintendods.ds_project.service;

import java.io.IOException;

public class UnicastListenService {
    private TCPClient client;

    public UnicastListenService() {
        Thread receiverThread = new Thread(() -> {
            try {
                client = new TCPClient();
            } catch (IOException e) {
                System.out.println("Error:\tconstructor\n" + e.toString() + "\n");
                throw new RuntimeException(e);
            }
        });
        receiverThread.start();
    }

    public void stop() {
        try {
            client.stop();
        } catch (IOException e) {
            System.out.println("Error:\tstop\n" + e.toString() + "\n");
            throw new RuntimeException(e);
        }
    }

}
