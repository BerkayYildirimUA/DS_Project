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

    public DashboardTab(RestTemplate restTemplate, FilesTab filesTab) {
        this.restTemplate = restTemplate;
        this.filesTab = filesTab; // Initialize FilesTab reference
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper

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

        String[] columnNames = {"Name", "Id", "IP", "Status"};
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
        }, 0, 5000); // Fetch nodes every 5 seconds
    }

    public void checkNodeStatus() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(NODES_URL, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                System.out.println(responseBody);
                List<Integer> ids = extractIdsFromJson(responseBody);
                System.out.println(ids);
                SwingUtilities.invokeLater(() -> updateServerStatus(true, ids.size()));
                for (int i = 0; i < ids.size(); i++) {
                    try {
                        ResponseEntity<String> response2 = restTemplate.getForEntity("http://localhost:8089/node/" + ids.get(i), String.class);
                        System.out.println(response2);
                        String ipAddress = extractIpAddress(response2.getBody());
                        List<String> returnValue = List.of("John", String.valueOf(ids.get(i)), ipAddress);
                        updateNodesTable(returnValue);

                    } catch (RestClientException e) {
                        e.printStackTrace();
                    }
                }
                SwingUtilities.invokeLater(() -> filesTab.updateFilesTable(ids)); // Update FilesTab
            } else {
                SwingUtilities.invokeLater(() -> updateServerStatus(false, 0));
            }
        } catch (RestClientException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> updateServerStatus(false, 0));
        }
    }

    /*private ClientNode[] parseNodes(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, ClientNode[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ClientNode[0]; // Return an empty array if parsing fails
        }
    }*/

    private void updateNodesTable(List<String> node) {
        if (!isNodeInTable(node.get(1))) {
            nodesTableModel.addRow(new Object[]{node.get(0), node.get(1), node.get(2), "online"}); // Assuming status is online for now
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

    private boolean isNodeInTable(String nodeId) {
        for (int i = 0; i < nodesTableModel.getRowCount(); i++) {
            if (nodesTableModel.getValueAt(i, 1).equals(nodeId)) {
                return true;
            }
        }
        return false;
    }
}
