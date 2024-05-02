package nintendods.ds_project.service;

import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private ServerSocket server;
    private DataInputStream dataIn;

    @Value("${tcp.unicast.port}")
    private int PORT;

    private boolean isRunning = true;

    public void connect() throws IOException {
        // TODO: Read PORT value from file
        if (PORT < 1024) PORT = 3780;
        server = new ServerSocket(PORT);
        server.setReuseAddress(true);
        System.out.println("TCPServer:\t Listening for clients...");
        String message = "";

        while (isRunning) {
            System.out.println("TCPServer:\t Data exchange");
            Socket client = server.accept();
            dataIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            message = dataIn.readUTF();
            System.out.println("TCPServer:\t " + message);
            isRunning = message.isEmpty();
        }

        if (NSAPIService.getAPI().hasAddress())
            NSAPIService.getAPI().executeErrorPatch("/nodes/" + message + "/error");
    }

    public void stop() throws IOException {
        isRunning = false;
    }
}
