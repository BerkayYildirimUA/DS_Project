package nintendods.ds_project.model.file.log;

import nintendods.ds_project.model.ABaseNode;

public class ALog {
    ABaseNode issuer; // The node that has issued the log file.
    long timestamp; // The timestamp of writing.
    eLog logType; // The type of logging.
    String message; // Some extra info for the logging. Is not mandetory.

    public ALog(ABaseNode issuer, eLog type, String message) {
        setIssuer(issuer);
        setType(type);
        setTimestamp(System.currentTimeMillis());
        setMessage(message);
    }

    public ABaseNode getIssuer() {
        return this.issuer;
    }

    private void setIssuer(ABaseNode issuer) {
        this.issuer = issuer;
    }

    public eLog getType() {
        return this.logType;
    }

    private void setType(eLog type) {
        this.logType = type;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    private void setTimestamp(long time) {
        this.timestamp = time;
    }

    public String getMessage() {
        return this.message;
    }

    private void setMessage(String message) {
        this.message = message;
    }
}