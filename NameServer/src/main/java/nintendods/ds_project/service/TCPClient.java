package nintendods.ds_project.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPClient {
    private Socket socket;
    private DataOutputStream dataOut;
    private boolean isRunning = true;

    public void connect(String ip) throws IOException {
        connect(ip, "NameServer: Hello, This is coming from Client!");
    }

    public void connect(String ip, String message) throws IOException {
        socket = new Socket(ip, 3780);
        System.out.println("TCPClient:\t Connection Successful!");

        dataOut = new DataOutputStream(socket.getOutputStream());

        while (isRunning) {
            dataOut.writeUTF(message);
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        if (dataOut != null) dataOut.close();
    }
}
