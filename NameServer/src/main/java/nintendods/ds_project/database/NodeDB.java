package nintendods.ds_project.database;

import com.google.gson.reflect.TypeToken;
import nintendods.ds_project.Exeptions.EntryNotInDBExeption;
import nintendods.ds_project.Exeptions.IDTakenExeption;
import nintendods.ds_project.utility.JsonConverter;
import nintendods.ds_project.utility.NameToHash;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.TreeMap;

@Repository
public class NodeDB {

    private TreeMap<Integer, String> nodeID_to_nodeIP;

    public NodeDB() {
        nodeID_to_nodeIP = new TreeMap<>();
    }

    private void put(Integer nodeID, String ip) {
        nodeID_to_nodeIP.put(nodeID, ip);
        this.saveDB();
    }
    /* --------------------------------- ADD --------------------------------- */

    /**
     * <p>Add a server to the list.</p>
     *
     *
     * <p>If it can't find an empty spot IDTakenExeption is thrown.</p>
     *
     * @param name the name of the node
     * @param ip   the ip of the node
     * @return ID of server
     * @throws IDTakenExeption if the name server is full
     */
    public Integer addNode(String name, String ip) throws IDTakenExeption {
        Integer nodeID = NameToHash.convert(name);

        if (!nodeID_to_nodeIP.containsKey(nodeID)) {
            this.put(nodeID, ip);
            return nodeID;
        }

        throw new IDTakenExeption();
    }

    /* --------------------------------- DELETE --------------------------------- */
    public void deleteNode(String ip) {
        nodeID_to_nodeIP.entrySet().removeIf(entry -> entry.getValue().equals(ip));
        this.saveDB();
    }

    public void deleteNode(int nodeID) {
        nodeID_to_nodeIP.entrySet().removeIf(entry -> entry.getKey().equals(nodeID));
        this.saveDB();
    }

    public void deleteNode(int nodeID, String ip) throws EntryNotInDBExeption {
        if (this.exists(nodeID, ip)) {
            nodeID_to_nodeIP.remove(nodeID);
        } else {
            throw new EntryNotInDBExeption();
        }
        this.saveDB();
    }

    /* --------------------------------- CHECK --------------------------------- */
    public boolean exists(int nodeID) {
        return nodeID_to_nodeIP.containsKey(nodeID);
    }

    public boolean exists(String ip) {
        return nodeID_to_nodeIP.containsValue(ip);
    }

    public boolean exists(int nodeID, String ip) {

        if (nodeID_to_nodeIP.containsKey(nodeID)) {
            return nodeID_to_nodeIP.get(nodeID).equals(ip);
        } else {
            return false;
        }
    }


    /* --------------------------------- GET --------------------------------- */
    public int getSize() {
        return this.nodeID_to_nodeIP.size();
    }

    public String getIPfromID(int ID) {return this.nodeID_to_nodeIP.getOrDefault(ID, "");}

    /**
     * Retrieves the IP address of the server with the hash closest to the given file name.
     *
     * @param name file name
     * @return ipp of server for the file
     */
    public String getClosestIpFromName(String name) {
        return nodeID_to_nodeIP.get(getClosestIdFromName(name));
    }

    /**
     * Retrieves the closest ID address of the server with the hash closest to the given file name.
     *
     * @param name file name
     * @return ID of server for the file
     */
    public int getClosestIdFromName(String name) {
        Integer tempID = NameToHash.convert(name);

        Integer floor = nodeID_to_nodeIP.floorKey(tempID);
        Integer ceiling = nodeID_to_nodeIP.ceilingKey(tempID);

        // if no upper key, then we loop back to beginning
        if (ceiling == null) {
            ceiling = nodeID_to_nodeIP.firstKey();
        }

        // if no lower key, then we loop to end
        if (floor == null) {
            floor = nodeID_to_nodeIP.lastKey();
        }

        int distToFloor = (tempID - floor + (NameToHash.MAX_NODES + 1)) % (NameToHash.MAX_NODES + 1); // Wrap-around distance to floor
        int distToCeiling = (ceiling - tempID + (NameToHash.MAX_NODES + 1)) % (NameToHash.MAX_NODES + 1); // Wrap-around distance to ceiling

        int closestKey;
        if (distToFloor <= distToCeiling) {
            closestKey = floor;
        } else {
            closestKey = ceiling;
        }

        return closestKey;
    }

    public void saveDB() {
        saveDB("NodeDB.json");
    }

    public void loadDB() {
        loadDB("NodeDB.json");
    }

    public void loadDB(String fileName) {
        JsonConverter jsonConverter = new JsonConverter(fileName);

        Type type = new TypeToken<TreeMap<Integer, String>>() {
        }.getType();

        nodeID_to_nodeIP = (TreeMap<Integer, String>) jsonConverter.fromFile(type);
    }

    public void saveDB(String fileName) {
        JsonConverter jsonConverter = new JsonConverter(fileName);

        jsonConverter.toFile(nodeID_to_nodeIP);
    }
}
