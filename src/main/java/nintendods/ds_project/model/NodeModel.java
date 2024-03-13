package nintendods.ds_project.model;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NodeModel extends ABaseNode{

    private int id;
    private List<String> localFiles;
    private List<String> remoteFiles;

    public NodeModel(int id, InetAddress address, int port) {
        super(address, port);
        this.id = id;
        this.localFiles = new ArrayList<>();
        this.remoteFiles = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public List<String> getLocalFiles() {
        return localFiles;
    }

    public List<String> getRemoteFiles() {
        return remoteFiles;
    }

    public void createFile(String fileName) {
        // Perform logic to create file
        // Communicate with the naming server here to register the file
        localFiles.add(fileName);
    }

    public void deleteFile(String fileName) {
        // Perform logic to delete file
        // Communicate with the naming server here to unregister the file
        localFiles.remove(fileName);
    }

}
