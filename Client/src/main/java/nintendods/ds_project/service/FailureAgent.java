package nintendods.ds_project.service;

import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.file.log.ALog;
import nintendods.ds_project.model.file.log.eLog;
import nintendods.ds_project.utility.ApiUtil;
import org.antlr.v4.runtime.misc.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FailureAgent implements Runnable, Serializable {
    private final String failingNodeId;
    private String currentNodeId;

    public FailureAgent(String failingNodeId, String currentNodeId) {
        this.failingNodeId = failingNodeId;
        this.currentNodeId = currentNodeId;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(byteIn)) {
            return in.readObject();
        }
    }

    public Optional<FailureAgent> setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;

        if (currentNodeId.equals(failingNodeId)) {
            return Optional.empty();
        }

        return Optional.of(this);
    }

    @Override
    public void run() {
        // Step 1: Read the file list of the current node
        FileDB dataBase = FileDBService.getFileDB();


        for (AFile entry : dataBase.getFiles()) {
            List<ALog> logs = entry.getLogs();
            Map<String, ALog> logMap = new HashMap<>();
            logMap.put("downloadLocation", null);
            logMap.put("fileReplicated", null);

            for (int i = logs.size() - 1; i >= 0; i--) {
                if (logs.get(i).getType().equals(eLog.downloadLocation) && logMap.get("downloadLocation") != null) {
                    logMap.put("downloadLocation", logs.get(i));
                }

                if (logs.get(i).getType().equals(eLog.fileReplicated) && logMap.get("fileReplicated") != null) {
                    logMap.put("fileReplicated", logs.get(i));
                }

                if (logMap.get("downloadLocation") != null && logMap.get("fileReplicated") != null) {
                    break;
                }
            }

            Map<String, Integer> logsReadedMap = new HashMap<>();
            logsReadedMap.put("downloadLocation", getIDsFromLogs(logMap.get("downloadLocation")));
            logsReadedMap.put("fileReplicated", getIDsFromLogs(logMap.get("fileReplicated")));

            if (logsReadedMap.get("downloadLocation").equals(Integer.valueOf(failingNodeId))){
                entry.setDownloadLocation(currentNodeId);
                // Thy become the primordial one, the progenitor of this file. Dethroning the old guard who has failed.
                // They shall spread thy spawn to new lands, and shall make certain a new generation shall spring forth if thy join the ancients.
            }

            if (logsReadedMap.get("fileReplicated").equals(Integer.valueOf(failingNodeId))){
                entry.setReplicated( false, failingNodeId); // Thy are not replicated anymore.
                // Replicate thy self to a new node, but remember, thy ID needs to be the prev one from the failed throne.
                //Thy old spawn may be lost, but a new genration shall take it's place.

            }

        }

        // Log: The FailureAgent has completed its run on this node
        System.out.println("FailureAgent completed on node: " + currentNodeId);
    }

    private Integer getIDsFromLogs(ALog log) {
        int id = -1;
        Pattern pattern = Pattern.compile("Node with ID: (\\d+)");
        Matcher matcher = pattern.matcher(log.getMessage());
        while (matcher.find()) {
            id = Integer.parseInt(matcher.group(1));
        }
        return id;
    }

    private void transferFile(String fileName) {
        // Step 2.1: Check if the new owner already has a copy of the file
        boolean newOwnerHasFile = checkIfNewOwnerHasFile(fileName);

        if (!newOwnerHasFile) {
            // Option 1: Transfer the file to the new owner
            System.out.println("Transferring file " + fileName + " to new owner");
            updateLogs(fileName, true);
        } else {
            // Option 2: Only update the log
            System.out.println("File " + fileName + " already exists with new owner. Updating logs only.");
            updateLogs(fileName, false);
        }
    }

    private boolean checkIfNewOwnerHasFile(String fileName) {
        // This method should check if the new owner already has the file
        // For the purpose of this example, we'll assume it returns false
        return false;
    }

    private void updateLogs(String fileName, boolean fileTransferred) {
        if (fileTransferred) {
            System.out.println("Log: File " + fileName + " transferred to new owner");
        } else {
            System.out.println("Log: Ownership of file " + fileName + " updated to new owner in logs");
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(this);
            return byteOut.toByteArray();
        }
    }
}
