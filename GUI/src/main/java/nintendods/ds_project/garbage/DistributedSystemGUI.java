package nintendods.ds_project.garbage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class DistributedSystemGUI {

    JFrame frame;
    private JTextField nodeIdField;
    private JList<String> nodeList;
    private DefaultListModel<String> nodeListModel;
    private JList<String> localFilesList;
    private DefaultListModel<String> localFilesListModel;
    private JList<String> replicatedFilesList;
    private DefaultListModel<String> replicatedFilesListModel;
    private JLabel configLabel;

    private Map<String, Node> nodes;

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
        nodes = new HashMap<>();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Distributed System Node Manager");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel lblNodeId = new JLabel("Node ID:");
        panel.add(lblNodeId);

        nodeIdField = new JTextField();
        panel.add(nodeIdField);
        nodeIdField.setColumns(10);

        JButton btnAddNode = new JButton("Add Node");
        btnAddNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNode();
            }
        });
        panel.add(btnAddNode);

        JButton btnRemoveNode = new JButton("Remove Node");
        btnRemoveNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeNode();
            }
        });
        panel.add(btnRemoveNode);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.3);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        nodeListModel = new DefaultListModel<>();
        nodeList = new JList<>(nodeListModel);
        nodeList.addListSelectionListener(e -> showNodeDetails());
        JScrollPane nodeScrollPane = new JScrollPane(nodeList);
        splitPane.setLeftComponent(nodeScrollPane);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        splitPane.setRightComponent(detailsPanel);

        JLabel lblLocalFiles = new JLabel("Local Files:");
        detailsPanel.add(lblLocalFiles);

        localFilesListModel = new DefaultListModel<>();
        localFilesList = new JList<>(localFilesListModel);
        JScrollPane localFilesScrollPane = new JScrollPane(localFilesList);
        detailsPanel.add(localFilesScrollPane);

        JLabel lblReplicatedFiles = new JLabel("Replicated Files:");
        detailsPanel.add(lblReplicatedFiles);

        replicatedFilesListModel = new DefaultListModel<>();
        replicatedFilesList = new JList<>(replicatedFilesListModel);
        JScrollPane replicatedFilesScrollPane = new JScrollPane(replicatedFilesList);
        detailsPanel.add(replicatedFilesScrollPane);

        configLabel = new JLabel("Previous ID: \nNext ID: ");
        detailsPanel.add(configLabel);
    }

    private void addNode() {
        String nodeId = nodeIdField.getText().trim();
        if (nodeId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Node ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nodes.containsKey(nodeId)) {
            JOptionPane.showMessageDialog(frame, "Node ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Node newNode = new Node(nodeId);
        nodes.put(nodeId, newNode);
        nodeListModel.addElement("Node " + nodeId);
        nodeIdField.setText("");
    }

    private void removeNode() {
        String selectedNode = nodeList.getSelectedValue();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(frame, "No node selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nodeId = selectedNode.split(" ")[1];
        nodes.remove(nodeId);
        nodeListModel.removeElement(selectedNode);
        localFilesListModel.clear();
        replicatedFilesListModel.clear();
        configLabel.setText("Previous ID: \nNext ID: ");
    }

    private void showNodeDetails() {
        String selectedNode = nodeList.getSelectedValue();
        if (selectedNode == null) {
            return;
        }

        String nodeId = selectedNode.split(" ")[1];
        Node node = nodes.get(nodeId);

        localFilesListModel.clear();
        for (String file : node.getLocalFiles()) {
            localFilesListModel.addElement(file);
        }

        replicatedFilesListModel.clear();
        for (String file : node.getReplicatedFiles()) {
            replicatedFilesListModel.addElement(file);
        }

        configLabel.setText(String.format("Previous ID: %s\nNext ID: %s", node.getPreviousId(), node.getNextId()));
    }

    private static class Node {
        private String nodeId;
        private java.util.List<String> localFiles;
        private java.util.List<String> replicatedFiles;
        private String previousId;
        private String nextId;

        public Node(String nodeId) {
            this.nodeId = nodeId;
            this.localFiles = new java.util.ArrayList<>();
            this.replicatedFiles = new java.util.ArrayList<>();
            this.previousId = "None";
            this.nextId = "None";

            // Sample data for demonstration purposes
            localFiles.add("file1.txt");
            localFiles.add("file2.txt");
            replicatedFiles.add("file3.txt");
            replicatedFiles.add("file4.txt");
        }

        public String getNodeId() {
            return nodeId;
        }

        public java.util.List<String> getLocalFiles() {
            return localFiles;
        }

        public java.util.List<String> getReplicatedFiles() {
            return replicatedFiles;
        }

        public String getPreviousId() {
            return previousId;
        }

        public String getNextId() {
            return nextId;
        }
    }
}
