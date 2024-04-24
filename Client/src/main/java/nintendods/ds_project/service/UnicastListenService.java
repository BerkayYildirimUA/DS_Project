package nintendods.ds_project.service;

import java.io.IOException;

public class UnicastListenService {
    private final TCPClient client;

    public UnicastListenService() {
        try {
            client = new TCPClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            client.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
