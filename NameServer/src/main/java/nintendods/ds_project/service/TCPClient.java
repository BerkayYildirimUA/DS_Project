package nintendods.ds_project.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {
    public void connect(String ip) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, 378), 1000);
        System.out.println("Connection Successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
        dataOut.writeUTF("Hello, This is coming from Client!");
        String serverMessage = dataIn.readUTF();
        System.out.println(serverMessage);

        dataIn.close();
        dataOut.close();
        socket.close();
    }
}
