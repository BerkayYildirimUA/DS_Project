package nintendods.ds_project.utility;

import org.springframework.http.ResponseEntity;

// beetje sketch, maar ik wil niet nog meer klasses maken
public class UtilClientAPI extends UtilNameServerAPI{

    static String GET_getFileLocation(String filename, String nodeID){
        checkNsObjectIsNotNull();

        String addres = UtilNameServerAPI.GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("GET from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }
    /*
    snap de functie niet van "addFile(@RequestParam("fileName") String fileName, @RequestParam("nodeIP") String nodeIP)"
    static String POST_addFile(String filename, String nodeIP){
        checkNsObjectIsNotNull();

        String addres = UtilNameServerAPI.GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("GET from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }

     */

    static String DELETE_file(String filename, String nodeID){
        checkNsObjectIsNotNull();

        String addres = UtilNameServerAPI.GET_NodeIPfromID(nodeID);

        String url = "http://" + addres + "/api/files/" + filename;
        logger.info("DELETE from: " + url);
        ResponseEntity<String> Response_FileAddressByName = restTemplate.getForEntity(url, String.class);
        return Response_FileAddressByName.getBody();
    }


}
