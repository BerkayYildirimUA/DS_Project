package nintendods.ds_project.utility;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.UNAMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

public class UtilNameServerAPI {

    protected static final Logger logger = LoggerFactory.getLogger(UtilNameServerAPI.class);
    protected static final RestTemplate restTemplate = new RestTemplate();
    protected static UNAMObject nsObject;
    protected static String nameSeverAdress;

    public static UNAMObject getNsObject() {
        return nsObject;
    }

    public static String removeLeadingSlash(String input) {
        if (input.startsWith("/")) {
            return input.substring(1);
        }
        return input;
    }

    public static void setNsObject(UNAMObject nsObject) {
        UtilNameServerAPI.nsObject = nsObject;
        nameSeverAdress = "http://" + nsObject.getNSAddress() + ":8089/";
    }

    protected static void checkNsObjectIsNotNull(){
        if (nsObject == null){
            logger.warn("Name Server Object not found");
        }
    }

    static String GET_FileAddressByName(String filename){
        checkNsObjectIsNotNull();

        String URL_FileAddressByName = nameSeverAdress + "/files/" + filename;
        logger.info("GET from: " + URL_FileAddressByName);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(URL_FileAddressByName, String.class);
        return Response_FileAddressByName.getBody();
    }

    static String GET_NodeIPfromID(String id){
        checkNsObjectIsNotNull();

        String URL_NodeIPfromID = nameSeverAdress + "/node/" + id;
        logger.info("GET from: " + URL_NodeIPfromID);
        ResponseEntity<String> Response_NodeIPfromID = restTemplate.getForEntity(URL_NodeIPfromID, String.class);
        return  removeLeadingSlash(Objects.requireNonNull(Response_NodeIPfromID.getBody()));
    }

    static String POST_Node(ClientNode newNode){
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

    static String PATCH_endError(String id){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);


        String URL_endError = nameSeverAdress + "/nodes/" + id + "/error";
        logger.info("PATCH to: " + URL_endError);
        ResponseEntity<String> response = restTemplate.exchange(URL_endError, HttpMethod.PATCH, request, String.class);
        logger.info("PATCH response: " + response.getBody());
        return response.getBody();
    }

    static String DELETE_FileById(String id){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id;
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    static String DELETE_DueToError(String id){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id + "/error";
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

    static String DELETE_DueToShutdown(String id){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String URL_deleteFileById = nameSeverAdress + "/nodes/" + id + "/shutdown";
        logger.info("DELETE to: " + URL_deleteFileById);
        ResponseEntity<String> response = restTemplate.exchange(URL_deleteFileById, HttpMethod.DELETE, request, String.class);
        logger.info("DELETE response: " + response.getBody());
        return response.getBody();
    }

}
