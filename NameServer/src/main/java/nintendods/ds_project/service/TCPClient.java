package nintendods.ds_project.service;

import java.io.*;
import java.net.Socket;

public class TCPClient {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private volatile boolean isRunning = false;

    public TCPClient(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            System.out.println("Connected");

            // takes input from terminal
            input = new DataInputStream(System.in);

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException i) {
            System.out.println("Error 1");
        }
    }

//    public String sendMessage(String msg) throws IOException {
//        out.println(msg);
//        String resp = in.readLine();
//        return resp;
//    }
//
//    public void stopConnection() throws IOException {
//        in.close();
//        out.close();
//        clientSocket.close();
//    }
}
