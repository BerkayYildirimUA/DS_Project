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


        System.out.println(nodeID_to_nodeIP.floorKey(tempID));
        System.out.println(nodeID_to_nodeIP.ceilingKey(tempID));

        return 0;
    }

}
