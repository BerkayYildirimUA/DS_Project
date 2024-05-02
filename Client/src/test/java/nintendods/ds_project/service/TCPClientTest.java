package nintendods.ds_project.service;

import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClientTest {

    /*
    * Setup:
    * ClientNode contains TCP server and listens
    * NameServer contains TCP client and sends
    *
    * Data exchange:
    * Error is received at API
    * NS sends TCP packets to CN
    * CN does a errorCheck API request
    * NS stops sending TCP packets
    * */

    @Test
    public void TCPCommunication() {
        TCPServer server = new TCPServer();
        TCPClient client = new TCPClient();

        Thread clientThread = new Thread(() -> {
            try {
                server.connect();
                server.stop();
                client.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();

        try {
            client.connect(InetAddress.getLocalHost().getHostAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

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
            dataOut.close();
            socket.close();
        }
    }
}
