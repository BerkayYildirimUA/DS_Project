package nintendods.ds_project.service;

import nintendods.ds_project.Client;
import nintendods.ds_project.database.FileDB;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.file.log.ALog;
import nintendods.ds_project.model.file.log.eLog;
import nintendods.ds_project.utility.ApiUtil;
import nintendods.ds_project.utility.NameToHash;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.context.ConfigurableApplicationContext;

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
    private FileTransceiverService fileTransceiverService;
    private ConfigurableApplicationContext context;

    public FailureAgent(String failingNodeId, String currentNodeId, FileTransceiverService fileTransceiverService, ConfigurableApplicationContext context) {
        this.failingNodeId = failingNodeId;
        this.currentNodeId = currentNodeId;
        this.fileTransceiverService = fileTransceiverService;
        this.context = context;
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

    public void setFileTransceiverService(FileTransceiverService fileTransceiverService){
        this.fileTransceiverService = fileTransceiverService;
    }

    public void setContext(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public void setContextAndFileTransceiverService(ConfigurableApplicationContext context){
        setContext(context);
        setFileTransceiverService(context.getBean(Client.class).getFileTransceiver());
    }

    @Override
    public void run() {
        // Step 1: Read the file list of the current node
        FileDB dataBase = FileDBService.getFileDB();


        for (AFile file : dataBase.getFiles()) {
            List<ALog> logs = file.getLogs();
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
                file.setDownloadLocation(currentNodeId);

                String ip = ApiUtil.NameServer_GET_NodeIPfromID(NameToHash.convert(file.getName()));
                fileTransceiverService.sendFile(file, ip);
                String id = ApiUtil.NameServer_GET_NodeIDfromIP(ip);
                file.setReplicated(true, id);

            } else if (logsReadedMap.get("fileReplicated").equals(Integer.valueOf(failingNodeId))){
                file.setReplicated( false, failingNodeId);
                String ip = ApiUtil.NameServer_GET_NodeIPfromID(NameToHash.convert(file.getName()));
                fileTransceiverService.sendFile(file, ip);
                String id = ApiUtil.NameServer_GET_NodeIDfromIP(ip);
                file.setReplicated(true, id);


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

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(this);
            return byteOut.toByteArray();
        }
    }
}
