package nintendods.ds_project.model.file.log;

public enum eLog{
    fileCreation, //A log when the file is created.
    fileTransfer, //A log when a file is transfered to a new filepath.
    fileRename,   //A log when a file is renamed.  
    newOwnerNode  //A log when a file gets a new owner node. This can happen if the file is replicated.
}