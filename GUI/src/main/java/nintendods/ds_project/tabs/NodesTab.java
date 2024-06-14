package nintendods.ds_project.tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class NodesTab {

    private JPanel panel;
    private JTextField nodeIdField;
    private JList<String> nodeList;
    private DefaultListModel<String> nodeListModel;
    private JList<String> localFilesList;
    private DefaultListModel<String> localFilesListModel;
    private JList<String> replicatedFilesList;
    private DefaultListModel<String> replicatedFilesListModel;
    private JLabel configLabel;

    private Map<String, Node> nodes;

    public NodesTab() {
        nodes = new HashMap<>();
        initialize();
    }

    private void initialize() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.add(topPanel, BorderLayout.NORTH);

        JLabel lblNodeId = new JLabel("Node ID:");
        topPanel.add(lblNodeId);

        nodeIdField = new JTextField();
        topPanel.add(nodeIdField);
        nodeIdField.setColumns(10);

        JButton btnAddNode = new JButton("Add Node");
        btnAddNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNode();
            }
        });
        topPanel.add(btnAddNode);

        JButton btnRemoveNode = new JButton("Remove Node");
        btnRemoveNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeNode();
            }
        });
        topPanel.add(btnRemoveNode);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.3);
        panel.add(splitPane, BorderLayout.CENTER);

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
            JOptionPane.showMessageDialog(panel, "Node ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nodes.containsKey(nodeId)) {
            JOptionPane.showMessageDialog(panel, "Node ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(panel, "No node selected.", "Error", JOptionPane.ERROR_MESSAGE);
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

    public JPanel getPanel() {
        return panel;
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
