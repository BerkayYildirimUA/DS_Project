package nintendods.ds_project.model;
import nintendods.ds_project.Exeptions.NameServerFullExeption;
import nintendods.ds_project.utility.NameToHash;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

public class NameServerDatabase {

    public NameServerDatabase() {
        nodeID_to_nodeIP = new TreeMap<>();
    }

    private final TreeMap<Integer, ABaseNode> nodeID_to_nodeIP;

    /**
     * <p>Add a server to the list.</p>
     *
     * <p>Since the ID is based on name, it's possible that 2 different names have the same Hash.
     * To not override a name that has come before we will move the new ID by 1 until it finds an empty spot.</p>
     *
     * <p>If it can't find an empty spot NameServerFullExeption is thrown.</p>
     *
     * @param node
     * @return ID of server
     * @throws NameServerFullExeption
     */
    public Integer addNode(ABaseNode node) throws NameServerFullExeption {
        String name = node.getName();
        Integer nodeID = NameToHash.convert(name);

        for (int i = 0; i < 32768; ++i) {
            if (!nodeID_to_nodeIP.containsKey(nodeID)) {
                nodeID_to_nodeIP.put(nodeID, node);
                return nodeID;
            }

            nodeID++;
            if (nodeID > 32768){
                nodeID = 0;
            }
        }

        throw new NameServerFullExeption();
    }

    public void deleteNodeByName(String name){
        nodeID_to_nodeIP.entrySet().removeIf(entry -> entry.getValue().getName().equals(name));
    }

    public void deleteNode(ABaseNode node){
        nodeID_to_nodeIP.entrySet().removeIf(entry -> entry.getValue().equals(node));
    }

    public boolean exists(ABaseNode node){
        return nodeID_to_nodeIP.containsKey(NameToHash.convert(node.getName()));
    }

    public ABaseNode getNodefromID(Integer nodeID){
        return nodeID_to_nodeIP.get(nodeID);
    }

    public InetAddress getClosestNodeIP(String name){
        return getNodefromID(getClosestNodeID(name)).getAddress();
    }


    public Set<ABaseNode> getNodefromName(String name){
        return nodeID_to_nodeIP.values().stream().filter(value -> value.getName().equals(name)).collect(Collectors.toSet());
    }

    /**
     * Retrieves the closest IP address of the server with the hash closest to the given file name.
     * <p> <b>WARNING! Just because you input the exact name of a server doesn't mean you will definitely get its IP back! </b></p>
     *
     * <p>See {@link #addNode(ABaseNode)} to learn on how servers are added and how their hashes are determined,
     * which causes the warning.</p>
     *  @param name file name
     * @return ipp of server for the file
     */
    public ABaseNode getClosestNode(String name){
        return getNodefromID(getClosestNodeID(name));
    }

    /**
     * Retrieves the closest ID address of the server with the hash closest to the given file name.
     * <p> <b>WARNING! Just because you input the exact name of a server doesn't mean you will definitely get its ID back! </b></p>
     *
     * <p>See {@link #addNode(ABaseNode)} to learn on how servers are added and how their hashes are determined,
     * which causes the warning.</p>
     *  @param name file name
     * @return ID of server for the file
     */
    public int getClosestNodeID(String name){
        Integer tempID = NameToHash.convert(name);

        Integer floor = nodeID_to_nodeIP.floorKey(tempID);
        Integer ceiling = nodeID_to_nodeIP.ceilingKey(tempID);

        if (ceiling == null) { // if no upper key, then we loop back to beginning
            ceiling = nodeID_to_nodeIP.firstKey();
        }

        if (floor == null) { // if no lower key, then we loop to end
            floor = nodeID_to_nodeIP.lastKey();
        }

        int distToFloor = (tempID - floor + 32768) % 32768; // Wrap-around distance to floor
        int distToCeiling = (ceiling - tempID + 32768) % 32768; // Wrap-around distance to ceiling

        int closestKey;
        if (distToFloor <= distToCeiling) {
            closestKey = floor;
        } else {
            closestKey = ceiling;
        }

        return closestKey;
    }
}
