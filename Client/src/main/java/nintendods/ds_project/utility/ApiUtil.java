package nintendods.ds_project.utility;

import nintendods.ds_project.config.ClientNodeConfig;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.UNAMObject;
import nintendods.ds_project.service.FailureAgent;
import nintendods.ds_project.service.SyncAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.google.gson.reflect.TypeToken;


import java.io.IOException;
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

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

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

    private static String removeLeadingSlash(String input) {
        if (input.startsWith("/")) {
            return input.substring(1);
        }
        return input;
    }

    private static void checkNsObjectIsNotNull() {
        if (nsObject == null) {
            logger.warn("Name Server Object not found");
        }
    }


// ------------------------------------------- NameServer API ----------------------------------------------------------------

    //TODO: write test, might not work
    public static String NameServer_GET_FileAddressByName(String filename) {
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

    public static String NameServer_GET_NodeIPfromID(String id) {
        return NameServer_GET_NodeIPfromID(Integer.valueOf(id));
    }

    public static String NameServer_GET_NodeIDfromIP(String ip) {
        checkNsObjectIsNotNull();

        String URL_NodeIPfromID = nameSeverAdress + "node/?ip=" + ip;
        logger.info("GET from: " + URL_NodeIPfromID);
        ResponseEntity<String> Response_NodeIPfromID = restTemplate.getForEntity(URL_NodeIPfromID, String.class);
        return removeLeadingSlash(Objects.requireNonNull(Response_NodeIPfromID.getBody()));
    }

    //TODO: write test, might not work
    public static String NameServer_POST_Node(ClientNode newNode) {
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
    public static String NameServer_PATCH_endError(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);


        String URL_endError = nameSeverAdress + "/nodes/" + id + "/error";
        logger.info("PATCH to: " + URL_endError);
        ResponseEntity<String> response = restTemplate.exchange(URL_endError, HttpMethod.PATCH, request, String.class);
        logger.info("PATCH response: " + response.getBody());
        return response.getBody();
    }

    public static String NameServer_PATCH_endError(int id) {
        return NameServer_PATCH_endError(Integer.toString(id));
    }

    //TODO: write test, might not work
    public static String NameServer_DELETE_FileById(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id;
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    public static String NameServer_DELETE_FileById(int id) {
        return NameServer_DELETE_FileById(Integer.toString(id));
    }

    //TODO: write test, might not work
    public static String NameServer_DELETE_DueToError(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id + "/error";
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    public static String NameServer_DELETE_DueToError(int id) {
        return NameServer_DELETE_DueToError(Integer.toString(id));
    }


    //TODO: write test, might not work
    public static String NameServer_DELETE_DueToShutdown(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id + "/shutdown";
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    public static String NameServer_DELETE_DueToShutdown(int id) {
        return NameServer_DELETE_DueToShutdown(Integer.toString(id));
    }


// ------------------------------------------- CLIENT API ----------------------------------------------------------------

    //TODO: write test, might not work

    public static String Client_GET_getFileLocation(String filename, String nodeID) {
        checkNsObjectIsNotNull();

        String addres = ApiUtil.NameServer_GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("GET from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }

    public static String Client_GET_getFileLocation(String filename, int nodeID){
        return Client_GET_getFileLocation(filename, Integer.toString(nodeID));
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

    public static String Client_DELETE_file(String filename, int nodeID){
        return Client_DELETE_file(filename, Integer.toString(nodeID));
    }

    //TODO: write test, might not work

    public static String Client_DELETE_file(String filename, String nodeID) {
        checkNsObjectIsNotNull();

        String addres = ApiUtil.NameServer_GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("DELETE from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }

    //TODO: write test, might not work
    public static boolean Client_PUT_changeMyNextNodesNeighbor(int prevNodeID, int prevNodePort, int nextNodeID) {
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

    public static boolean Client_PUT_changeMyPrevNodesNeighbor(int nextNodeID, int nextNodePort, int prevNodeID) {
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

    public static boolean Client_Put_changeFileOwner(String fileName, String absolutePath, int nodeID){
        return Client_Put_changeFileOwner(fileName, absolutePath, Integer.toString(nodeID));
    }

    public static boolean Client_Put_changeFileOwner(String fileName, String absolutePath, String nodeID){

        String ip = NameServer_GET_NodeIPfromID(nodeID);

        String url = "Http://" + ip + ":" + ClientNodeConfig.getApiPort() + "/api/files/" + fileName + "/downloadLocation";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String requestBody = String.format("{\"absolutePath\":\"%s\", \"nodeID\":%s}", absolutePath, nodeID);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );
        if (response.getStatusCode().is2xxSuccessful()){
            return true;
        } else {
            return false;
        }
    }

    public static void Client_PUT_sendFailureAgent(FailureAgent agent, String nodeID) throws IOException {
        String ip = NameServer_GET_NodeIPfromID(nodeID);
        byte[] agentData = agent.serialize();

        String url = "Http://" + ip + ":" + ClientNodeConfig.getApiPort() + "/api/agent/failure";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(agentData, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class

        );

        System.out.println("Response: " + response.getBody());


    }

    public static void Client_POST_createFailureAgent(String failedNodeID, String sendNodeID) throws IOException {
        String ip = NameServer_GET_NodeIPfromID(sendNodeID);

        String url = "Http://" + ip + ":" + ClientNodeConfig.getApiPort() + "/api/agent/failure/?ID=" + failedNodeID;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class

        );

        System.out.println("Response: " + response.getBody());


    }


}
