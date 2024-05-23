package nintendods.ds_project.utility;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.SyncAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApiUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ApiUtil.class);
    protected static RestTemplate restTemplate = new RestTemplate();
    protected static UNAMObject nsObject;
    protected static String nameSeverAdress;
    protected static JsonConverter jsonConverter = new JsonConverter();

    public static UNAMObject getNsObject() {
        return nsObject;
    }

    public static void setNsObject(UNAMObject nsObject) {
        ApiUtil.nsObject = nsObject;
        nameSeverAdress = "http://" + nsObject.getNSAddress() + ":8089/";
    }

    public static void setRestTemplate(RestTemplate mockRestTemplate) {
        restTemplate = mockRestTemplate;
    }

    public static String removeLeadingSlash(String input) {
        if (input.startsWith("/")) {
            return input.substring(1);
        }
        return input;
    }

    protected static void checkNsObjectIsNotNull() {
        if (nsObject == null) {
            logger.warn("Name Server Object not found");
        }
    }


// ------------------------------------------- NameServer API ----------------------------------------------------------------

    //TODO: write test, might not work
    static String NameServer_GET_FileAddressByName(String filename) {
        checkNsObjectIsNotNull();

        String URL_FileAddressByName = nameSeverAdress + "/files/" + filename;
        logger.info("GET from: " + URL_FileAddressByName);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(URL_FileAddressByName, String.class);
        return Response_FileAddressByName.getBody();
    }

    //TODO: write test, might not work
    public static String NameServer_GET_NodeIPfromID(int id) {
        checkNsObjectIsNotNull();

        String URL_NodeIPfromID = nameSeverAdress + "node/" + Integer.toString(id);
        logger.info("GET from: " + URL_NodeIPfromID);
        ResponseEntity<String> Response_NodeIPfromID = restTemplate.getForEntity(URL_NodeIPfromID, String.class);
        return removeLeadingSlash(Objects.requireNonNull(Response_NodeIPfromID.getBody()));
    }

    //TODO: write test, might not work
    static String NameServer_POST_Node(ClientNode newNode) {
        checkNsObjectIsNotNull();

        JsonConverter jsonConverter = new JsonConverter("test");
        String nodeAsJson = jsonConverter.toJson(newNode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(nodeAsJson, headers);

        String URL_Nodes = nameSeverAdress + "/nodes";
        logger.info("POST to: " + URL_Nodes);

        ResponseEntity<String> response = restTemplate.postForEntity(URL_Nodes, request, String.class);
        logger.info("Post response: " + response.getBody());
        return response.getBody();
    }

    //TODO: write test, might not work
    static String NameServer_PATCH_endError(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);


        String URL_endError = nameSeverAdress + "/nodes/" + id + "/error";
        logger.info("PATCH to: " + URL_endError);
        ResponseEntity<String> response = restTemplate.exchange(URL_endError, HttpMethod.PATCH, request, String.class);
        logger.info("PATCH response: " + response.getBody());
        return response.getBody();
    }

    //TODO: write test, might not work
    static String NameServer_DELETE_FileById(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id;
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    //TODO: write test, might not work
    static String NameServer_DELETE_DueToError(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id + "/error";
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    //TODO: write test, might not work
    static String NameServer_DELETE_DueToShutdown(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id + "/shutdown";
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

// ------------------------------------------- CLIENT API ----------------------------------------------------------------

    //TODO: write test, might not work
    static String Client_GET_getFileLocation(String filename, int nodeID) {
        checkNsObjectIsNotNull();

        String addres = ApiUtil.NameServer_GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("GET from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }

    /*
    snap de functie niet van "addFile(@RequestParam("fileName") String fileName, @RequestParam("nodeIP") String nodeIP)"
    static String POST_addFile(String filename, String nodeIP){
        checkNsObjectIsNotNull();

        String addres = ApiUtil.GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("GET from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }

     */

    //TODO: write test, might not work
    static String Client_DELETE_file(String filename, int nodeID) {
        checkNsObjectIsNotNull();

        String addres = ApiUtil.NameServer_GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("DELETE from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }

    //TODO: write test, might not work
    static boolean Client_PUT_changeMyNextNodesNeighbor(int prevNodeID, int prevNodePort, int nextNodeID) {
        checkNsObjectIsNotNull();
        String prevNodeIP = ApiUtil.NameServer_GET_NodeIPfromID(prevNodeID);

        String UrlForPrevNode = "http://" + prevNodeIP + ":" + prevNodePort + "/api/Management/nextNodeID/?ID=" + nextNodeID;
        logger.info("PUT to: " + UrlForPrevNode);

        try {
            restTemplate.put(UrlForPrevNode, String.class);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    static boolean Client_PUT_changeMyPrevNodesNeighbor(int nextNodeID, int nextNodePort, int prevNodeID) {
        ApiUtil.checkNsObjectIsNotNull();

        String nextNodeIP = ApiUtil.NameServer_GET_NodeIPfromID(nextNodeID);
        String urlForNextNode = "http://" + nextNodeIP + ":" + nextNodePort + "/api/Management/prevNodeID/?ID=" + prevNodeID;
        logger.info("PUT to: " + urlForNextNode);
        try {
            restTemplate.put(urlForNextNode, String.class);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static List<AFile> clientGetAllFiles(String address, int port) {
        String url = "http://" + address + ":" + Integer.toString(port) + "/api/files";
        logger.info("GET from: " + url);
        ResponseEntity<String> files = restTemplate.getForEntity(url, String.class);
        Type localFileListType = new TypeToken<ArrayList<AFile>>() {}.getType();
        return (List<AFile>) jsonConverter.toObject(files.getBody(), localFileListType);

        //Beter to use WebClient? RestTemplate wil be deprecated .
    }

    public static Map<String, Boolean> getSyncAgentFiles(String address, int port) {

        String url = "http://" + address + ":" + Integer.toString(port) + "/api/agent/sync";
        logger.info("GET from: " + url);

        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity(url, String.class);
        Type syncAgentFileListType = new TypeToken<HashMap<String, Boolean>>() {}.getType();
        return (Map<String, Boolean>) jsonConverter.toObject(responseEntityStr.getBody(),syncAgentFileListType);

        //Beter to use WebClient? RestTemplate wil be deprecated .
    }
}
