package nintendods.ds_project.service;

import nintendods.ds_project.database.NodeDB;

/**
 * Singleton service for accessing the node database.
 */
public class NodeDBService {
    private static NodeDB nodeDB = null;

    public static NodeDB getNodeDB() {
        if(nodeDB == null)
            nodeDB = new NodeDB();

        return nodeDB;
    }
}
