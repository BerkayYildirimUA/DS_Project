package nintendods.ds_project.model.file.log;

public enum eLog{
    fileCreation, //A log when the file is created.
    fileTransfer, //A log when a file is transfered over TCP.
    newCopyNode,  //A log when a file gets a new copy node eg: a replication has happend.
    newOwnerNode  //A log when a file gets a new owner node.
}