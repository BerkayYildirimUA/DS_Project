package nintendods.ds_project.model.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.file.log.ALog;
import nintendods.ds_project.model.file.log.eLog;
import nintendods.ds_project.utility.NameToHash;

public class AFile
{
    String path; //A path (absolute)
    String name; //A name (with extention)
    int id;  //the hashed id of the name
    List<ALog> logs; //All the logs that happend with the file
    ABaseNode owner;    //The base owner of the file where the file has its origine.
    ABaseNode replicatedOwner;  //The replicated Owner where the file is located

    /**
     * Create a new file object that creates a log with the provided ABaseNode. It finds the file based on the absolute path.
     * @param path the absolute path of the given file with the file name included
     * @param name the name of the file 
     * @param owner the ABaseNode object that we want to set as the owner of the file
     */
    public AFile(String path, String name, ABaseNode creator){
        setName(name);
        setPath(path);
        logs = new ArrayList<>();
        this.owner = creator;

        //Set a new log of initial creation with the owner of the file
        logs.add(new ALog(owner, eLog.fileCreation, "initial creation of the file"));
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
    public AFile(String path, String name, ABaseNode creator, List<ALog> prevList){
    }

    /**
     * Set the name of the file with the extension included.
     * Automatically generated the ID based on the file's name
     * @param name
     */
    public void setName(String name){
        this.name = name;
        this.id = NameToHash.convert(this.name);
    }
    public String getName(){
        return this.name;
    }

    /**
     * Set the path of the file with the file included
     * @param path
     */
    public void setPath(String path){
        this.path = path;
    }
    public String getPath(){
        return this.path;
    }

    /**
     * Returns the id of the file based on the name of the file.
     * @return
     */
    public int getId(){
        return this.id;
    }

    public File getFile(){
        return new File(this.path);
    }
}