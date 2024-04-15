package nintendods.ds_project.model.message;

// Enum defining different types of messages used in the system
public enum eMessageTypes{
    MulticastNode, // Message sent to multiple nodes
    UnicastNodeToNode, // Message sent from one node to another
    UnicastNamingServerToNode // Message sent from the naming server to a node
}
