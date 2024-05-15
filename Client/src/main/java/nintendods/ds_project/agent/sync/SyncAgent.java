package nintendods.ds_project.agent.sync;


import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.JsonConverter;

public class SyncAgent implements Runnable, Serializable {
    private Map<String, Boolean> files = new HashMap<String,Boolean>() {}; // Map of all files in the system and the possible lock on it.

    public SyncAgent(Map<String, Boolean> files){
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
            Type fileListType = new TypeToken<ArrayList<AFile>>() {}.getType();

            //Load all files of the node
            List<AFile> allFiles =  (List<AFile>) jsonConverter.toObject(ApiUtil.clientGetAllFiles(InetAddress.getLocalHost().getHostAddress(), ClientNodeConfig.API_PORT), fileListType);

            //Only keep the one that are not replicated.
            List<AFile> ownedFiles = allFiles.stream().filter(f -> f.isReplicated() == false).toList();

            //Check if a file is present in the sync agent database.
            for (AFile f : ownedFiles) {

                if(!this.files.containsKey(f.getName())){
                    //If not, add to the database   
                    this.files.put(f.getName(), false);
                }
                //else, do nothing.
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void processLockRequest() {
        //Check the requests of a lock
        String fileNameLockRequest = Data.getFirstLockRequest();
        if(fileNameLockRequest != null){
            //There is a lock request present

            //Check if the lock is present on the global files.
            if (!this.files.getOrDefault(fileNameLockRequest, true)){
                //Lock is present so add back to queue
                Data.requestLock(fileNameLockRequest);
            }
            else{
                //No Lock is present so set lock
                if(Data.addAcceptedLock(fileNameLockRequest))
                    this.files.replace(fileNameLockRequest, false, true);
                else{
                    //Failed to put the data on queue
                    //Set back on queue
                    Data.requestLock(fileNameLockRequest);
                }
            }
        }
    }

    private void processUnlockRequest() {
        //Check the requests of an unlock
        String fileNameUnlockRequest = Data.getFirstUnlockRequest();
        if(fileNameUnlockRequest != null){
            //There is a lock request present

            //Check if the lock is present on the global files.
            if (this.files.getOrDefault(fileNameUnlockRequest, false)){
                //Lock is present so remove lock
                this.files.replace(fileNameUnlockRequest, true, false);
            }
            //else, No Lock is is present so do nothing
        }
    }

    public Map<String, Boolean> getFiles(){
        return this.files;
    }
}
