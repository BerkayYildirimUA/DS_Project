package nintendods.ds_project.service;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.gson.reflect.TypeToken;

import nintendods.ds_project.Client;
import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.syncAgent.Data;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.JsonConverter;

public class SyncAgent implements Runnable, Serializable {

    @Autowired
    ConfigurableApplicationContext context;

    private Map<String, Boolean> files = new HashMap<String, Boolean>() {
    }; // Map of all files in the system and the possible lock on it.

    public SyncAgent() {
        this.files = new HashMap<String, Boolean>() {
        };
    }

    public SyncAgent(Map<String, Boolean> files) {
        this.files = files;
    }

    @Override
    public void run() {
        syncFiles();
        processLockRequest();
        processUnlockRequest();
        return;
    }

    private void syncFiles() {
        try {
            JsonConverter jsonConverter = new JsonConverter();
            Type localFileListType = new TypeToken<ArrayList<AFile>>() {}.getType();
            Type syncAgentFileListType = new TypeToken<HashMap<String, Boolean>>() {}.getType();

            // Load all files of the local node
            List<AFile> allFiles = (List<AFile>) jsonConverter.toObject(
                    ApiUtil.clientGetAllFiles(InetAddress.getLocalHost().getHostAddress(), ClientNodeConfig.API_PORT),
                    localFileListType);

            // Only keep the one that are not replicated (so owned by the node itself).
            List<AFile> ownedFiles = allFiles.stream().filter(f -> f.isReplicated() == false).toList();

            // update local files
            updateFileList(ownedFiles);

            // Get the next node ip from naming server
            Client client = context.getBean(Client.class);
            int nextNodeId = client.getNode().getNextNodeId();
            String nextNodeIP = ApiUtil.NameServer_GET_NodeIPfromID(nextNodeId);

            //get the files from the next nodes of that sync agent
            String nextNodeFileString = ApiUtil.getSyncAgentFiles(nextNodeIP, ClientNodeConfig.API_PORT);

            // convert to a map
            Map<String, Boolean> nextNodeAllFiles = (Map<String, Boolean>) jsonConverter.toObject(nextNodeFileString,
                    syncAgentFileListType);

            // Check if we have new files on the next node that are not present on the
            // current node.

            for (String nextNodeFileName : nextNodeAllFiles.keySet()) {
                if (!this.files.containsKey(nextNodeFileName)) {
                    // the file is not present in the local files so add to the local file with the lock of the next node
                    this.files.put(nextNodeFileName, nextNodeAllFiles.get(nextNodeFileName));
                } else {
                    // File is present so check the lock value and update accordingly
                    // this.files.put(nextNodeFileName, nextNodeAllFiles.get(nextNodeAllFiles));

                    boolean nextNodeLock = nextNodeAllFiles.get(nextNodeFileName);
                    if (nextNodeLock == true && this.files.get(nextNodeFileName)) { // If both are locked, do nothing
                        System.out.print("SyncAgent - both files are locked, do nothing");
                    } else if (nextNodeLock == false && this.files.get(nextNodeFileName)) {
                        System.out.print("SyncAgent - local file is locked and next node file not, keep lock file");
                    } else {
                        System.out.print("SyncAgent - local file lock is updated with next node file lock");
                        this.files.put(nextNodeFileName, nextNodeAllFiles.get(nextNodeFileName));
                    }
                }
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateFileList(List<AFile> ownedFiles) {
        // Check if a file is present in the sync agent database.
        for (AFile f : ownedFiles) {

            if (!this.files.containsKey(f.getName())) {
                // If not, add to the database
                this.files.put(f.getName(), false);
            }
            // else, do nothing.
        }
    }

    private void processLockRequest() {
        // Check the requests of a lock
        String fileNameLockRequest = Data.getFirstLockRequest();
        if (fileNameLockRequest != null) {
            // There is a lock request present

            // Check if the lock is present on the global files.
            if (!this.files.getOrDefault(fileNameLockRequest, true)) {
                // Lock is present so add back to queue
                Data.requestLock(fileNameLockRequest);
            } else {
                // No Lock is present so set lock
                if (Data.addAcceptedLock(fileNameLockRequest))
                    this.files.replace(fileNameLockRequest, false, true);
                else {
                    // Failed to put the data on queue
                    // Set back on queue
                    Data.requestLock(fileNameLockRequest);
                }
            }
        }
    }

    private void processUnlockRequest() {
        // Check the requests of an unlock
        String fileNameUnlockRequest = Data.getFirstUnlockRequest();
        if (fileNameUnlockRequest != null) {
            // There is a lock request present

            // Check if the lock is present on the global files.
            if (this.files.getOrDefault(fileNameUnlockRequest, false)) {
                // Lock is present so remove lock
                this.files.replace(fileNameUnlockRequest, true, false);
            }
            // else, No Lock is is present so do nothing
        }
    }

    public Map<String, Boolean> getFiles() {
        return this.files;
    }
}
