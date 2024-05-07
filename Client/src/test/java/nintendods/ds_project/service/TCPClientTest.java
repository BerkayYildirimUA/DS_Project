package nintendods.ds_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@SpringBootTest
public class TCPClientTest {

    @Test
    public void TCPCommunication() { //werkt allen als ik Intelij als admin run --Berkay
        TCPServer server = new TCPServer(3780);
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
            if (dataOut != null) dataOut.close();
            if (socket != null) socket.close();
        }
    }
}
