package nintendods.ds_project.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import nintendods.ds_project.helper.NameToHash;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.util.HashMap;


public class NameServerDatabase {

    private HashMap<Integer, InetAddress> nodeID_to_nodeIP = new HashMap<>();

    public Integer addNode(String name, InetAddress inetAddress){
        Integer nodeID = NameToHash.convert(name);
        nodeID_to_nodeIP.put(nodeID, inetAddress);
        return nodeID;
    }

    public InetAddress getNodeIP(Integer nodeID){
        return nodeID_to_nodeIP.get(nodeID);
    }

    public int getNodeID(String name){


        return 0;
    }

}
