package nintendods.ds_project.service;

import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private ServerSocket server;
    private DataInputStream dataIn;

    private int PORT;
    private boolean keepListening = true;
    private String message = "";

    public TCPServer(@Value("${tcp.unicast.port}") int port) {
        PORT = port;
    }

    public void connect() throws IOException {
        System.out.println("TCPServer:\t Listening for clients...");
        server = new ServerSocket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(PORT));
    }

    public void listen() throws IOException {
        System.out.println("TCPServer:\t Listening for message...");

        while (keepListening) {
            System.out.println("TCPServer:\t Data exchange");
            Socket client = server.accept();
            dataIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            message = dataIn.readUTF();
            System.out.println("TCPServer:\t " + message);
            keepListening = message.isEmpty(); // Keeps running as long as nothing is received
        }
    }

    public Pair<Integer, Integer> decryptMessage() {
        if (message.isEmpty())
            return null;
        else {
            String[] parts = message.split("->");
            message = "";
            keepListening = true;
            return new Pair<>(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
    }

    public void stop() throws IOException {
        keepListening = false;
        if (dataIn != null) dataIn.close();
        if (server != null) server.close();
    }
}
