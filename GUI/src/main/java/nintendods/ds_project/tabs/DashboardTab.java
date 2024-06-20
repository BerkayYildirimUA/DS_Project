package nintendods.ds_project.tabs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nintendods.ds_project.model.Node;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashboardTab {

    private static final String NODES_URL = "http://localhost:8089/nodes";

    private JPanel panel;
    private JLabel statusLabel;
    private JLabel nodesCountLabel;
    private JLabel discoveryStatusLabel;
    private JTable nodesTable;
    private DefaultTableModel nodesTableModel;
    private Timer timer;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FilesTab filesTab; // Reference to FilesTab

    private List<String> ipAddresses;
    private static final int[] PORTS = {8083, 8084, 8085, 8086}; // Ports to try

    public DashboardTab(RestTemplate restTemplate, FilesTab filesTab) {
        this.restTemplate = restTemplate;
        this.filesTab = filesTab; // Initialize FilesTab reference
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper

        ipAddresses = new ArrayList<>();

        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        JPanel nameServerPanel = new JPanel();
        nameServerPanel.setLayout(new GridLayout(3, 2));
        nameServerPanel.setBorder(BorderFactory.createTitledBorder("Nameserver"));

        nameServerPanel.add(new JLabel("Status:"));
        statusLabel = new JLabel("-");
        nameServerPanel.add(statusLabel);

        nameServerPanel.add(new JLabel("Nodes:"));
        nodesCountLabel = new JLabel("-");
        nameServerPanel.add(nodesCountLabel);

        nameServerPanel.add(new JLabel("Discovery:"));
        discoveryStatusLabel = new JLabel("-");
        nameServerPanel.add(discoveryStatusLabel);

        panel.add(nameServerPanel, BorderLayout.NORTH);

        String[] columnNames = {"Name", "Id", "IP", "Status", "NextNodeID", "PreviousNodeID"};
        nodesTableModel = new DefaultTableModel(columnNames, 0);
        nodesTable = new JTable(nodesTableModel);
        JScrollPane nodesScrollPane = new JScrollPane(nodesTable);
        nodesScrollPane.setBorder(BorderFactory.createTitledBorder("Nodes"));

        panel.add(nodesScrollPane, BorderLayout.CENTER);

        startFetchingNodes();
    }

    private void startFetchingNodes() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkNodeStatus();
            }
        }, 0, 10000); // Fetch nodes every 10 seconds
    }

    public void checkNodeStatus() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(NODES_URL, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                List<Integer> ids = extractIdsFromJson(responseBody);
                ipAddresses.clear();
                SwingUtilities.invokeLater(() -> updateServerStatus(true, ids.size()));
                for (int id : ids) {
                    try {
                        ResponseEntity<String> response2 = restTemplate.getForEntity("http://localhost:8089/node/" + id, String.class);
                        String ipAddress = extractIpAddress(response2.getBody());
                        ipAddresses.add(ipAddress); // Collect IP addresses

                        boolean serviceFound = false;
                        String nodeName = "";
                        int previousNodeID = -1;
                        int nextNodeID = -1;

                        for (int port : PORTS) {
                            try {
                                ResponseEntity<String> response3 = restTemplate.getForEntity("http://" + ipAddress + ":" + port + "/api/Management/name/", String.class);
                                nodeName = extractName(response3.getBody());
                                previousNodeID = extractPrevNodeId(response3.getBody());
                                nextNodeID = extractNextNodeId(response3.getBody());

                                List<String> returnValue = List.of(nodeName, String.valueOf(id), ipAddress, "online", String.valueOf(nextNodeID), String.valueOf(previousNodeID));
                                updateNodesTable(returnValue);
                                serviceFound = true;
                            } catch (Exception e) {
                                System.out.println("No service at port " + port + " for IP " + ipAddress);
                            }
                        }
                        if (!serviceFound) {
                            List<String> returnValue = List.of("", String.valueOf(id), ipAddress, "offline", "-1", "-1");
                            updateNodesTable(returnValue);
                        }
                    } catch (RestClientException e) {
                        e.printStackTrace();
                    }
                }
                if (filesTab != null)
                    SwingUtilities.invokeLater(() -> filesTab.updateFilesTable(ipAddresses)); // Pass IP addresses to FilesTab
            } else {
                SwingUtilities.invokeLater(() -> updateServerStatus(false, 0));
                updateNodesToOffline();
            }
        } catch (RestClientException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> updateServerStatus(false, 0));
            updateNodesToOffline();
        }
    }

    public List<String> getIpAddresses() {
        return new ArrayList<>(ipAddresses);
    }

    private void updateNodesTable(List<String> node) {
        boolean nodeExists = false;
        for (int i = 0; i < nodesTableModel.getRowCount(); i++) {
            if (nodesTableModel.getValueAt(i, 1).equals(node.get(1))) {
                nodeExists = true;
                if (node.get(3).equals("online")) {
                    nodesTableModel.setValueAt(node.get(0), i, 0); // Name
                    nodesTableModel.setValueAt(node.get(2), i, 2); // IP
                    nodesTableModel.setValueAt(node.get(3), i, 3); // Status
                    nodesTableModel.setValueAt(node.get(4), i, 4); // NextNodeID
                    nodesTableModel.setValueAt(node.get(5), i, 5); // PreviousNodeID
                } else {
                    nodesTableModel.setValueAt(node.get(3), i, 3); // Status (offline)
                }
                break;
            }
        }

        if (!nodeExists) {
            nodesTableModel.addRow(new Object[]{node.get(0), node.get(1), node.get(2), node.get(3), node.get(4), node.get(5)});
        }
    }

    private void updateNodesToOffline() {
        for (int i = 0; i < nodesTableModel.getRowCount(); i++) {
            nodesTableModel.setValueAt("offline", i, 3); // Set status to offline
        }
    }

    private void updateServerStatus(boolean isOnline, int nodeCount) {
        statusLabel.setText(isOnline ? "online" : "offline");
        nodesCountLabel.setText(isOnline ? String.valueOf(nodeCount) : "0");
        discoveryStatusLabel.setText(isOnline ? "enabled" : "disabled");
    }

    public JPanel getPanel() {
        return panel;
    }

    public static List<Integer> extractIdsFromJson(String json) {
        List<Integer> idList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Node> nodes = objectMapper.readValue(json, new TypeReference<List<Node>>() {});
            for (Node node : nodes) {
                idList.add(node.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idList;
    }

    public static String extractIpAddress(String message) {
        String ipPattern = "/(\\d+\\.\\d+\\.\\d+\\.\\d+)";
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static String extractName(String message) {
        String namePattern = "name='([^']*)'";
        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static int extractPrevNodeId(String message) {
        String prevNodeIdPattern = "prevNodeId=(\\d+)";
        Pattern pattern = Pattern.compile(prevNodeIdPattern);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1; // Return -1 if not found, or handle as needed
        }
    }

    public static int extractNextNodeId(String message) {
        String nextNodeIdPattern = "nextNodeId=(\\d+)";
        Pattern pattern = Pattern.compile(nextNodeIdPattern);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1; // Return -1 if not found, or handle as needed
        }
    }

    private boolean isNodeInTable(String nodeId) {
        for (int i = 0; i < nodesTableModel.getRowCount(); i++) {
            if (nodesTableModel.getValueAt(i, 1).equals(nodeId)) {
                return true;
            }
        }
        return false;
    }
}
