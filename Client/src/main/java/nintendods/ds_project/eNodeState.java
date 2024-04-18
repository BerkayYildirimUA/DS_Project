package nintendods.ds_project;

/**
 * Enumerates possible states for network nodes within the system.
 */
public enum eNodeState {
    Discovery, // Node is discovering other nodes
    Listening, // Node is listening for incoming messages
    Transfer, // Node is transferring data
    Shutdown, // Node is in the process of shutting down
    Error // Node has encountered an error
}
