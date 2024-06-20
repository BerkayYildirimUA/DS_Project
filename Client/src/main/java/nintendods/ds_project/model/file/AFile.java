package nintendods.ds_project.model.file;

import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.log.ALog;
import nintendods.ds_project.model.file.log.eLog;
import nintendods.ds_project.utility.NameToHash;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AFile implements Serializable {
    String path = ""; // The path
    String name = ""; // A filename (with extention)
    int id = -1; // the hashed id of the filename
    List<ALog> logs; // All the logs that happend with the file
    ANode owner = null; // The owner of the file where the file is hosted.
    boolean isBeenBackedUp = false; // If the file has been replicated, does it have a copy? will be false for the copy itself

    /**
     * Create a new file object that creates a log with the provided ABaseNode. It
     * finds the file based on the absolute path.
     * 
     * @param path    the absolute path of the given file with the file name
     *                included
     * @param name    the name of the file
     * @param creator the ABaseNode object that we want to set as the owner of the
     *                file
     */
    public AFile(String path, String name, ANode creator) {
        logs = new ArrayList<>();
        this.owner = creator;
        this.name = name;
        setId(name);
        this.path = path;

        // Set a new log of initial creation with the owner of the file
        logs.add(new ALog(getOwner(), eLog.fileCreation, "initial creation of the file"));
        logs.add(new ALog(getOwner(), eLog.downloadLocation, "Initial Download location is Node with ID: " + NameToHash.convert(creator.getName())));

        setReplicated(false, "-1");
    }

    /**
     * Set the name of the file with the extension included.
     * Automatically generated the ID based on the file's name
     * 
     * @param name
     */
    public void setName(String name) {
        if (!name.equals(getName())) {
            logs.add(new ALog(getOwner(), eLog.fileRename, "Rename the file from " + getName() + " to " + name + " ."));
        }

        this.name = name;
        setId(name);
    }

    public String getName() {
        return this.name;
    }

    /**
     * Set the path of the file with the file included. Creates a log when the path
     * is changed.
     * 
     * @param path
     */
    public void setPath(String path) {
        if (!getAbsolutePath().equals(path)) {
            logs.add(new ALog(getOwner(), eLog.fileTransfer,
                    "File has been transfered from " + getAbsolutePath() + " to " + path + " ."));
        }
        this.path = path;
    }

    /**
     * Get the directory path of the file with no file name.
     * 
     * @return
     */
    public String getDirPath() {
        File temp = getFile();
        String parentPath = temp.getParent(); // Get the parent directory

        System.out.println(parentPath);
        return parentPath;
    }

    /**
     * Get the directory path + filename. So the URL to the file.
     * 
     * @return
     */
    public String getAbsolutePath() {
        return this.path;
    }

    /**
     * Returns the id of the file based on the name of the file.
     * 
     * @return
     */
    public int getId() {
        return this.id;
    }

    private void setId(String name) {
        this.id = NameToHash.convert(this.name);
    }

    public File getFile() {
        return new File(getAbsolutePath());
    }

    public void setOwner(ANode node) {
        if (!getOwner().getName().equals(node.getName()) && getOwner() != null) {
            logs.add(new ALog(getOwner(), eLog.newOwnerNode,
                    "Owner has changed from " + getOwner().getName() + " to " + node.getName() + " ."));
        }
        this.owner = node;
    }

    public ANode getOwner() {
        return this.owner;
    }

    public ANode getCreator() {
        if (!this.logs.isEmpty()) {
            if (this.logs.get(0).getType() == eLog.fileCreation) {
                return this.logs.get(0).getIssuer();
            }
            return (ANode) this.logs.stream().filter(l -> l.getType() == eLog.fileCreation).toList().get(0).getIssuer();
        }
        return null;
    }

    public List<ALog> getLogs() {
        return this.logs;
    }

    public String getFormattedLogs() {
        String text = "";
        if (logs.isEmpty())
            text = "No logs";
        else {
            for (ALog iLog : this.logs) {

                String temp = "Date: "
                        + LocalDateTime.ofInstant(Instant.ofEpochMilli(iLog.getTimestamp()), ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MM:dd HH:mm:ss"))
                        + "\tType: " + iLog.getType().toString()
                        + "\r\nIssuer: " + iLog.getIssuer().getName()
                        + "\tInfo: " + iLog.getMessage();

                text += temp + "\r\n";
            }
        }
        return text;
    }

    public void setReplicated(boolean hasBeenBackedUp, String nodeID) { // --> I am a original, this is location of copy

        if (!isBeenBackedUp() && hasBeenBackedUp) {
            logs.add(new ALog(getOwner(), eLog.fileReplicated,
                    "The file is replicated to node with ID: " + nodeID));
        } else if (isBeenBackedUp() && !hasBeenBackedUp){
            logs.add(new ALog(getOwner(), eLog.fileReplicated,
                    "The file is no longer replicated"));
        } else if (!isBeenBackedUp() && !hasBeenBackedUp){
            logs.add(new ALog(getOwner(), eLog.fileReplicated,
                    "The file is not replicated"));
        }
        this.isBeenBackedUp = hasBeenBackedUp;
    }

    public void setDownloadLocation(String ID){ // --> I am a copy, this is location of original
        logs.add(new ALog(getOwner(), eLog.downloadLocation, "Download location changed to Node with ID: " + ID));
    }

    public boolean isBeenBackedUp() {
        return this.isBeenBackedUp;
    }


}