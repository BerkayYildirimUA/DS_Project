package nintendods.ds_project.tabs;

//import nintendods.ds_project.controller.ClientFileAPI;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FilesTab {

    private JPanel panel;
    private JTable filesTable;
    private DefaultTableModel filesTableModel;
    private final RestTemplate restTemplate;

    public FilesTab(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        String[] columnNames = {"File Name", "ID", "Location"};
        filesTableModel = new DefaultTableModel(columnNames, 0);
        filesTable = new JTable(filesTableModel);
        JScrollPane filesScrollPane = new JScrollPane(filesTable);
        filesScrollPane.setBorder(BorderFactory.createTitledBorder("Files"));

        panel.add(filesScrollPane, BorderLayout.CENTER);
    }

    public void updateFilesTable(List<Integer> nodeIds) {
        filesTableModel.setRowCount(0); // Clear the table before updating
        for (Integer nodeId : nodeIds) {
            fetchAndAddFiles(nodeId);
        }
    }

    private void fetchAndAddFiles(Integer nodeId) {
        try {
            String url = "http://localhost:8089/api/files"; // Adjust if the endpoint differs
            String response = restTemplate.getForObject(url, String.class);
            // Parse the response and update the table model
            // Assuming the response is a JSON array of file objects
            // You would need to parse it similarly to how you did in DashboardTab

            // For simplicity, let's assume we get a list of files with their names and locations
            List<String[]> files = parseFiles(response); // You need to implement parseFiles

            for (String[] file : files) {
                filesTableModel.addRow(new Object[]{file[0], nodeId, file[1]});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String[]> parseFiles(String json) {
        // Implement this method to parse the JSON response and return a list of file details
        // For now, let's return a dummy list
        return List.of(
                new String[]{"file1.txt", "/path/to/file1"},
                new String[]{"file2.txt", "/path/to/file2"}
        );
    }

    public JPanel getPanel() {
        return panel;
    }
}
