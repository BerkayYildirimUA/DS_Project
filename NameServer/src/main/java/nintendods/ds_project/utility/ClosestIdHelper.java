package nintendods.ds_project.utility;

import nintendods.ds_project.model.NodeModel;

import java.util.List;
import java.util.Objects;

public class ClosestIdHelper {
    public static NodeModel getClosestNode(List<NodeModel> nodes, String name) {
        if (nodes.isEmpty()) return null;

        int hash_id = Objects.hash(name); // Vervang met robbe hashing functie
        NodeModel closestNode = nodes.getFirst();
        int minDif = Math.abs(closestNode.getId() - hash_id);

        for (NodeModel node: nodes) {
            int dif = Math.abs(node.getId() - hash_id);
            if (dif < minDif) {
                minDif = dif;
                closestNode = node;
            }
        }

        return closestNode;
    }
}
