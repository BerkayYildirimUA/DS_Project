package nintendods.ds_project.tabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FilesTab {

    private JPanel panel;
    private JTable filesTable;
    private DefaultTableModel filesTableModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final int[] PORTS = {8083, 8084, 8085, 8086}; // Ports to try
    private Timer timer;
    private final DashboardTab dashboardTab; // Reference to DashboardTab

    public FilesTab(RestTemplate restTemplate, DashboardTab dashboardTab) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.dashboardTab = dashboardTab;

        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        String[] columnNames = {"File Name", "ID", "Location", "Owner ID"};
        filesTableModel = new DefaultTableModel(columnNames, 0);
        filesTable = new JTable(filesTableModel);
        filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane filesScrollPane = new JScrollPane(filesTable);
        filesScrollPane.setBorder(BorderFactory.createTitledBorder("Files"));

        panel.add(filesScrollPane, BorderLayout.CENTER);

        filesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && filesTable.getSelectedRow() != -1) {
                int selectedRow = filesTable.getSelectedRow();
                int fileId = (Integer) filesTableModel.getValueAt(selectedRow, 1);

                String fileDetails = fetchFileDetails(fileId); // Fetch detailed file information
                if (fileDetails != null) {
                    String details = parseFileDetails(fileDetails);
                    JOptionPane.showMessageDialog(panel, details);
                }
            }
        });

        startUpdatingFilesTable();
    }

    private void startUpdatingFilesTable() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> ipAddresses = dashboardTab.getIpAddresses();
                updateFilesTable(ipAddresses);
            }
        }, 0, 20000); // Run every 10 seconds
    }

    public void updateFilesTable(List<String> ipAddresses) {
        filesTableModel.setRowCount(0); // Clear the table before updating
        for (String ipAddress : ipAddresses) {
            fetchAndAddFiles(ipAddress);
        }
    }

    private void fetchAndAddFiles(String ipAddress) {
        boolean anyServiceFound = false;
        for (int port : PORTS) {
            try {
                String url = "http://" + ipAddress + ":" + port + "/api/files"; // Use IP address and port
                String response = restTemplate.getForObject(url, String.class);
                //System.out.println(response);

                JsonNode files = parseFiles(response);
                for (JsonNode file : files) {
                    String fileName = file.get("name").asText();
                    int fileId = file.get("id").asInt();
                    String location = file.get("path").asText();
                    int ownerId = file.get("owner").get("id").asInt();

                    if (!isLocationInTable(location)) {
                        filesTableModel.addRow(new Object[]{fileName, fileId, location, ownerId});
                    }
                }
                anyServiceFound = true;
            } catch (Exception e) {
                System.out.println("No service at port " + port + " for IP " + ipAddress);
            }
        }
        if (!anyServiceFound) {
            System.out.println("No services found for IP " + ipAddress + " on ports 8083, 8084, 8085, 8086.");
        }
    }

    private boolean isLocationInTable(String location) {
        for (int i = 0; i < filesTableModel.getRowCount(); i++) {
            if (filesTableModel.getValueAt(i, 2).equals(location)) {
                return true;
            }
        }
        return false;
    }

    private JsonNode parseFiles(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String fetchFileDetails(int fileId) {
        try {
            // Assuming all detailed file information is fetched from the same IP used earlier
            // Adjust if necessary
            String url = "http://localhost:8084/api/files/" + fileId; // Adjust if the endpoint differs
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseFileDetails(String fileDetails) {
        try {
            JsonNode file = objectMapper.readTree(fileDetails);

            StringBuilder details = new StringBuilder();
            details.append("File Name: ").append(file.get("name").asText()).append("\n");
            details.append("ID: ").append(file.get("id").asInt()).append("\n");
            details.append("Location: ").append(file.get("path").asText()).append("\n");
            details.append("Owner ID: ").append(file.get("owner").get("id").asInt()).append("\n");
            details.append("Logs:\n");

            for (JsonNode log : file.get("logs")) {
                details.append("  - ").append(log.get("logType").asText()).append(" by Issuer ID: ").append(log.get("issuer").get("id").asInt())
                        .append(" at ").append(log.get("timestamp").asLong()).append(": ").append(log.get("message").asText()).append("\n");
            }
            return details.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing file details";
        }
    }

    public JPanel getPanel() {
        return panel;
    }
}
