package nintendods.ds_project.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPClient {
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;

    public TCPClient() throws IOException {
        serverSocket = new ServerSocket(378);
        System.out.println("Listening for clients...");

        clientSocket = serverSocket.accept();

        dataIn = new DataInputStream(clientSocket.getInputStream());
        dataOut = new DataOutputStream(clientSocket.getOutputStream());

        String clientMessage = dataIn.readUTF();
        System.out.println(clientMessage);
        String serverMessage = "Hi this is coming from Server!";
        dataOut.writeUTF(serverMessage);
    }

    public void stop() throws IOException {
        dataIn.close();
        dataOut.close();
        serverSocket.close();
        clientSocket.close();
    }

}
