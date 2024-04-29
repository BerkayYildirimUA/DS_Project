package nintendods.ds_project.service;

import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.utility.NameToHash;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ReplicationService {
    private final FileTransceiverService fileTransceiverService;

    public ReplicationService(FileTransceiverService fileTransceiverService) {
        this.fileTransceiverService = fileTransceiverService;
    }

    private void replicateFile(AFile file, ClientNode destinationNode) {
        boolean success = fileTransceiverService.sendFile(file, destinationNode.getAddress());
        if (success) {
            System.out.println("File: " + file.getName() + " send to :" + destinationNode.getAddress());
        } else {
            System.err.println("Failed to replicate file: " + file.getName());
        }
    }
}
