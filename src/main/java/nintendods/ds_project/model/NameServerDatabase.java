package nintendods.ds_project.model;
import nintendods.ds_project.helper.NameToHash;

import java.net.InetAddress;
import java.util.*;


public class NameServerDatabase {

    private TreeMap<Integer, InetAddress> nodeID_to_nodeIP = new TreeMap<>();

    public Integer addNode(String name, InetAddress inetAddress){
        Integer nodeID = NameToHash.convert(name);
        System.out.println(name + ": " + nodeID);
        nodeID_to_nodeIP.put(nodeID, inetAddress);
        return nodeID;
    }

    public InetAddress getNodeIP(Integer nodeID){
        return nodeID_to_nodeIP.get(nodeID);
    }

    public int getNodeID(String name){
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
