package nintendods.ds_project.model.file.log;

import nintendods.ds_project.model.ABaseNode;

public class ALog {
    ABaseNode client;   // The node that has issued the log file.
    long timestamp;     // The timestamp of writing.
    eLog logType;       // The type of logging.
    String message;     // Some extra info for the logging. Is not mandetory.
}