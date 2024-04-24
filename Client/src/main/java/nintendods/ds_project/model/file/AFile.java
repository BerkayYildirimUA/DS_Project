package nintendods.ds_project.model.file;

import java.util.ArrayList;
import java.util.List;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.file.log.ALog;

public class AFile
{
    String path; //A path (absolute)
    String name; //A name (with extention)
    String pathAndName; //Full File Path with the name afterwards
    int fileId;  //the hashed id of the name
    List<ALog> logs; //All the logs that happend with the file
    ABaseNode owner;    //The base owner of the file where the file has its origine.
    ABaseNode replicatedOwner;  //The replicated Owner where the file is located

    /**
     * Create a new file object that creates an empty log.
     * @param path the absolute path of the given file
     * @param name the name of the file 
     */
    public AFile(String path, String name){

        logs = new ArrayList<>();
    }

    /**
     * Create a new file object that creates a log with the provided ABaseNode.
     * @param path the absolute path of the given file
     * @param name the name of the file 
     * @param owner the ABaseNode object that we want to set as the owner of the file
     */
    public AFile(String path, String name, ABaseNode owner){
        logs = new ArrayList<>();
        this.owner = owner;
    }

    /**
     * Create a new file object that creates a log with the provided owner of the file, 
     * the log of the file and the new replication node of the file
     * @param path
     * @param name
     * @param prevOwner
     * @param prevList
     * @param replicatedOwner
     */
    public AFile(String path, String name, ABaseNode prevOwner, List<ALog> prevList, ABaseNode replicatedOwner){
        this.logs = prevList;
        this.owner = prevOwner;
        this.replicatedOwner = replicatedOwner;
    }

    public void setName(String name){
        this.name = name;

    }
    public String getName(){
        return this.name;
    }

    public void setPath(String name){
        
    }
    public String getPath(){
        
    }
}