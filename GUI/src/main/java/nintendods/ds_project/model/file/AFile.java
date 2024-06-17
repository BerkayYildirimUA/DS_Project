package nintendods.ds_project.model.file;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AFile {
    private String path;
    private String name;
    private int id;
    private List<Log> logs;
    private Owner owner;

    @JsonProperty("isBeenBackedUp")
    private boolean beenBackedUp;

    public AFile() {
    }

    public AFile(String path, String name, int id, List<Log> logs, Owner owner, boolean beenBackedUp) {
        this.path = path;
        this.name = name;
        this.id = id;
        this.logs = logs;
        this.owner = owner;
        this.beenBackedUp = beenBackedUp;
    }

    // Getters and setters...

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public boolean isBeenBackedUp() {
        return beenBackedUp;
    }

    public void setBeenBackedUp(boolean beenBackedUp) {
        this.beenBackedUp = beenBackedUp;
    }

    public static class Log {
        private Issuer issuer;
        private long timestamp;
        private String logType;
        private String message;

        // Getters and setters...
        public Issuer getIssuer() {
            return issuer;
        }

        public void setIssuer(Issuer issuer) {
            this.issuer = issuer;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getLogType() {
            return logType;
        }

        public void setLogType(String logType) {
            this.logType = logType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class Issuer {
        private int id;
        private int prevNodeId;
        private int nextNodeId;
        private String address;
        private int port;
        private String name;

        // Getters and setters...
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPrevNodeId() {
            return prevNodeId;
        }

        public void setPrevNodeId(int prevNodeId) {
            this.prevNodeId = prevNodeId;
        }

        public int getNextNodeId() {
            return nextNodeId;
        }

        public void setNextNodeId(int nextNodeId) {
            this.nextNodeId = nextNodeId;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Owner {
        private int id;
        private int prevNodeId;
        private int nextNodeId;
        private String address;
        private int port;
        private String name;

        // Getters and setters...
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPrevNodeId() {
            return prevNodeId;
        }

        public void setPrevNodeId(int prevNodeId) {
            this.prevNodeId = prevNodeId;
        }

        public int getNextNodeId() {
            return nextNodeId;
        }

        public void setNextNodeId(int nextNodeId) {
            this.nextNodeId = nextNodeId;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
