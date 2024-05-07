package nintendods.ds_project.service;

import com.google.gson.Gson;
import nintendods.ds_project.model.ClientNode;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TCPTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void TCPCommunication() {
        TCPServer server = new TCPServer(3780);
        TCPClient client = new TCPClient();

        Thread clientThread = new Thread(() -> {
            try {
                server.connect();
                server.listen();
                client.stop();
                server.stop();
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

    private static final int NODE_NAME_LENGTH = 20;
    private static final int NODE_GLOBAL_PORT = 21;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    @Test
    public void Error2NodesCommunication() throws Exception {
        var ref = new Object() { boolean completed = false; };

        // Creat nodes
        ArrayList<ClientNode> nodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                nodes.add(new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH)));
            }
        } catch (UnknownHostException e) {
            System.out.println("Error2NodesCommunication: Create nodes");
            throw new RuntimeException(e);
        }

        // Sort nodes
        nodes.sort(Comparator.comparing(ClientNode::getId));

        // Put nodes in ring topology
        System.out.println("Error2NodesCommunication: Node Id " + nodes.get(0).getId());
        nodes.get(0).setPrevNodeId(nodes.get(1).getId());
        nodes.get(0).setNextNodeId(nodes.get(1).getId());
        System.out.println("Error2NodesCommunication: Node Id " + nodes.get(1).getId());
        nodes.get(1).setPrevNodeId(nodes.get(0).getId());
        nodes.get(1).setNextNodeId(nodes.get(0).getId());

        Gson gson = new Gson();
        for (ClientNode node: nodes) {
            mockMvc.perform(MockMvcRequestBuilders
                    .post("/nodes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(node))
            ).andExpect(MockMvcResultMatchers.status().isOk());
            Thread.sleep(378);
        }

        // Setup API service
        NSAPIService API = NSAPIService.getAPI();
        try {
            API.setIp(InetAddress.getLocalHost().getHostAddress());
            API.setPort(8089);
        } catch (UnknownHostException e) {
            System.out.println("Error2NodesCommunication: Setup API");
            throw new RuntimeException(e);
        }

        // Create TCP server (ClientNode)
        UnicastListenerService unicastListener = new UnicastListenerService(3780);

        // 2 nodes listen 1 enters error state
        Thread listenThread = new Thread(() -> {
            while (!ref.completed) {
                unicastListener.listenAndUpdate(nodes.get(1));
            }
        });
        listenThread.start();
        System.out.println("Error2NodesCommunication: Delete node with id " + nodes.get(0).getId());
        // API.executeErrorDelete("/nodes/" + nodes.get(0).getId() + "/error");
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/nodes/" + nodes.get(0).getId() + "/error")
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Wait for nodes to be up to data
        while (!ref.completed) {
            ref.completed = nodes.get(1).getPrevNodeId() == nodes.get(1).getId();
        }
        unicastListener.stopListening();

        System.out.println("Error2NodesCommunication: Previous node " + nodes.get(0).getPrevNodeId());
        assertEquals(nodes.get(1).getPrevNodeId(), nodes.get(1).getId());
        System.out.println("Error2NodesCommunication: Next node " + nodes.get(0).getNextNodeId());
        assertEquals(nodes.get(1).getNextNodeId(), nodes.get(1).getId());
    }

    @Test
    public void Error3NodesCommunication() throws Exception {
        var ref = new Object() { boolean completed = false; };

        // Creat nodes
        ArrayList<ClientNode> nodes = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                nodes.add(new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, generateRandomString(NODE_NAME_LENGTH)));
            }
        } catch (UnknownHostException e) {
            System.out.println("Error3NodesCommunication: Create Clients");
            throw new RuntimeException(e);
        }

        // Sort nodes
        nodes.sort(Comparator.comparing(ClientNode::getId));

        // Put nodes in ring topology
        System.out.println("Error3NodesCommunication: Node Id " + nodes.get(0).getId());
        nodes.get(0).setPrevNodeId(nodes.get(2).getId());
        nodes.get(0).setNextNodeId(nodes.get(1).getId());
        System.out.println("Error3NodesCommunication: Node Id " + nodes.get(1).getId());
        nodes.get(1).setPrevNodeId(nodes.get(0).getId());
        nodes.get(1).setNextNodeId(nodes.get(2).getId());
        System.out.println("Error3NodesCommunication: Node Id " + nodes.get(2).getId());
        nodes.get(2).setPrevNodeId(nodes.get(1).getId());
        nodes.get(2).setNextNodeId(nodes.get(0).getId());

        Gson gson = new Gson();
        for (ClientNode node: nodes) {
            mockMvc.perform(MockMvcRequestBuilders
                    .post("/nodes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(gson.toJson(node))
            ).andExpect(MockMvcResultMatchers.status().isOk());
            Thread.sleep(378);
        }

        // Setup API service
        NSAPIService API = NSAPIService.getAPI();
        try {
            API.setIp(InetAddress.getLocalHost().getHostAddress());
            API.setPort(8089);
        } catch (UnknownHostException e) {
            System.out.println("Error3NodesCommunication: Setup API");
            throw new RuntimeException(e);
        }

        // Create TCP server (ClientNode)
        UnicastListenerService unicastListener = new UnicastListenerService(3780);

        // 2 nodes listen 1 enters error state
        Thread listenThread = new Thread(() -> {
            while (!ref.completed) {
                unicastListener.listenAndUpdate(nodes.get(1));
                unicastListener.listenAndUpdate(nodes.get(2));
            }
        });
        listenThread.start();
        System.out.println("Error3NodesCommunication: Delete node with id " + nodes.get(0).getId());
        // API.executeErrorDelete("/nodes/" + nodes.get(0).getId() + "/error");
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/nodes/" + nodes.get(0).getId() + "/error")
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Wait for nodes to be up to data
        while (!ref.completed) {
            ref.completed = nodes.get(1).getPrevNodeId() == nodes.get(2).getId() &&
                            nodes.get(2).getPrevNodeId() == nodes.get(1).getId();
        }
        unicastListener.stopListening();

        System.out.println("Error3NodesCommunication: Previous node " + nodes.get(1).getPrevNodeId());
        assertEquals(nodes.get(1).getPrevNodeId(), nodes.get(2).getId());
        System.out.println("Error3NodesCommunication: Next node " + nodes.get(1).getNextNodeId());
        assertEquals(nodes.get(1).getNextNodeId(), nodes.get(2).getId());

        System.out.println("Error2NodesCommunication: Previous node " + nodes.get(2).getPrevNodeId());
        assertEquals(nodes.get(2).getPrevNodeId(), nodes.get(1).getId());
        System.out.println("Error2NodesCommunication: Next node " + nodes.get(2).getNextNodeId());
        assertEquals(nodes.get(2).getNextNodeId(), nodes.get(1).getId());
    }

    public static class NSAPIService {
        private String ip;
        private int port;

        private static NSAPIService API = null;
        public static NSAPIService getAPI() {
            if (API == null)
                API = new NSAPIService();

            return API;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPort(int port) {
            this.port = port;
        }

        private String getBaseUrl () {
            return "http://" + ip + ":" + port;
        }

        public boolean hasAddress() { return ip != null && port != 0; }

        public String executeErrorDelete(String path) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(getBaseUrl() + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setUseCaches(false);
                connection.setDoOutput(true);


                int responseCode = connection.getResponseCode();
                // System.out.println("GET Response Code :: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    //System.out.println("NSAPIService: id=" + response);
                    return response.toString();
                } else {
                    //System.out.println("NSAPIService: DELETE request did not work.");
                    return "";
                }
            } catch (Exception e) {
                System.out.println("NSAPIService: executeErrorDelete");
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        public String executeErrorPatch(String path) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(getBaseUrl() + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setUseCaches(false);
                connection.setDoOutput(true);


                int responseCode = connection.getResponseCode();
                // System.out.println("GET Response Code :: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    //System.out.println("NSAPIService: id=" + response);
                    return response.toString();
                } else {
                    //System.out.println("NSAPIService: PATCH request did not work.");
                    return "";
                }
            } catch (Exception e) {
                System.out.println("NSAPIService: executeErrorPatch");
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
    public class UnicastListenerService {
        private TCPServer server;

        public UnicastListenerService(@Value("${tcp.unicast.port}") int port) {
            this.server = new TCPServer(port);
            Thread receiverThread = new Thread(() -> {
                try {
                    server.connect();
                    server.listen();
                } catch (IOException e) {
                    System.out.println("UnicastListenService - Error:\tconstructor\n" + e + "\n");
                    throw new RuntimeException(e);
                }
            });
            receiverThread.start();
        }

        public void listenAndUpdate(ClientNode node) {
            Pair<Integer, Integer> newIdConfig = server.decryptMessage();
            if (newIdConfig != null) {
                if (node.getPrevNodeId() == newIdConfig.a && node.getNextNodeId() == newIdConfig.a) {
                    // 2 Nodes in the topology of which one is in error state
                    node.setPrevNodeId(-1);
                    node.setNextNodeId(-1);
                } else if (node.getPrevNodeId() == newIdConfig.a) {
                    // Previous node is in error state
                    node.setPrevNodeId(newIdConfig.b);
                } else {
                    // Next node is in error state
                    node.setNextNodeId(newIdConfig.b);
                }

                //if (NSAPIService.getAPI().hasAddress())
                //    NSAPIService.getAPI().executeErrorPatch("/nodes/" + node.getId() + "/error");
                try {
                    mockMvc.perform(MockMvcRequestBuilders
                            .patch("/nodes/" + node.getId() + "/error")
                    ).andExpect(MockMvcResultMatchers.status().isOk());
                } catch (Exception e) {
                    System.out.println("UnicastListenerService: listenAndUpdate");
                    throw new RuntimeException(e);
                }
            }
        }

        public void stopListening() {
            try {
                server.stop();
            } catch (IOException e) {
                System.out.println("UnicastListenService - Error:\tstop\n" + e + "\n");
                throw new RuntimeException(e);
            }
        }

    }
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
            server = new ServerSocket(PORT);
            server.setReuseAddress(true);
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
}
