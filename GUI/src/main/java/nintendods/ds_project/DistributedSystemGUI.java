package nintendods.ds_project;

import nintendods.ds_project.tabs.DashboardTab;
import nintendods.ds_project.tabs.FilesTab;
import nintendods.ds_project.tabs.NodesTab;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;

public class DistributedSystemGUI {

    JFrame frame;

    private RestTemplate restTemplate;


    public static void start() {
        EventQueue.invokeLater(() -> {
            try {
                DistributedSystemGUI window = new DistributedSystemGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DistributedSystemGUI() {
        initialize();
    }

    private void initialize() {
        restTemplate = new RestTemplate();
        frame = new JFrame();
        frame.setTitle("Distributed System Node Manager");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Add tabs
        FilesTab filesTab = new FilesTab(restTemplate);
        tabbedPane.addTab("Dashboard", new DashboardTab(restTemplate, filesTab).getPanel());
        tabbedPane.addTab("Nodes", new NodesTab().getPanel());
        tabbedPane.addTab("Files", filesTab.getPanel());
    }

    public static void main(String[] args) {
        start();
    }
}
