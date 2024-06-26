package nintendods.ds_project.utility;

import com.google.gson.reflect.TypeToken;
import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.ClientNode;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class JsonTests {

    @Test
    public void checkSimpleConverter(){
        JsonConverter jsonParser = new JsonConverter("file.json");
        ABaseNode node;
        try {
            node = new ClientNode(InetAddress.getByName("127.0.0.1"), 20, "Node1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        };

        File myObj = new File("file.json");
        myObj.delete();

        assertEquals("{\"id\":16960,\"address\":\"127.0.0.1\",\"port\":20,\"name\":\"Node1\"}", jsonParser.toJson(node));
    }

    @Test
    public void checkSimpleParse(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        String json = "{\"address\":\"127.0.0.1\",\"port\":19}";
        ClientNode node = (ClientNode) jsonParser.toObject(json, ClientNode.class);

        File myObj = new File("file.json");
        myObj.delete();

        assertEquals(19, node.getPort());
    }

    @Test
    public void checkFileSave(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        String json = "{\"address\":\"127.0.0.1\",\"port\":19}";
        jsonParser.toFile(json);
        String data = "";
        try {
            File myObj = new File("file.json");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data = data + myReader.nextLine();
            }
            myReader.close();

            myObj.delete();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        assertEquals(json, data);
    }

    @Test
    public void checkFileRead(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        ABaseNode node;
        try {
            node = new ClientNode(InetAddress.getByName("127.0.0.1"), 20,"Node1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        };

        jsonParser.toFile(node);

        ABaseNode nodeFromFile = (ClientNode) jsonParser.fromFile(ClientNode.class);

        File myObj = new File("file.json");
        myObj.delete();

        assertEquals(node.getAddress(), nodeFromFile.getAddress());
        assertEquals(node.getPort(), nodeFromFile.getPort());


    }

    @Test
    public void checkTamperedFile(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        File file = new File("file.json");
        file.delete();

        String json = "{\"address\":\"127.0.0.1\",\"port\":19}";
        jsonParser.toFile(json);

        file.delete();

        ABaseNode node = (ClientNode)jsonParser.fromFile(ClientNode.class);
        assertNull(node);

        File myObj = new File("file.json");
        myObj.delete();
    }

    @Test
    public void checkListOfObjects(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        List<ABaseNode> listOfNodes = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++)
                listOfNodes.add(new ClientNode(InetAddress.getByName(String.format("127.0.0.%d", i)), 20+i,String.format("node%d", i)));

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String expected = "[{\"id\":17185,\"address\":\"127.0.0.0\",\"port\":20,\"name\":\"node0\"},{\"id\":17185,\"address\":\"127.0.0.1\",\"port\":21,\"name\":\"node1\"},{\"id\":17185,\"address\":\"127.0.0.2\",\"port\":22,\"name\":\"node2\"},{\"id\":17185,\"address\":\"127.0.0.3\",\"port\":23,\"name\":\"node3\"}]";

        String jsonString = jsonParser.toJson(listOfNodes);

        assertEquals(expected, jsonString);
    }

    @Test
    public void checkListOfObjectsRevert(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        List<ABaseNode> listOfNodes = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++)
                listOfNodes.add(new ClientNode(InetAddress.getByName(String.format("127.0.0.%d", i)), 20+i,String.format("node%d", i)));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String expected = "[{\"address\":\"127.0.0.1\",\"port\":20},{\"address\":\"127.0.0.2\",\"port\":21},{\"address\":\"127.0.0.3\",\"port\":22},{\"address\":\"127.0.0.4\",\"port\":23}]";

        Type type = new TypeToken<ArrayList<ClientNode>>() {}.getType();

        List<ABaseNode> listOfNodesReceived = (List<ABaseNode>) jsonParser.toObject(expected, type);

        File myObj = new File("file.json");
        myObj.delete();

        assertEquals(listOfNodes.size(), listOfNodesReceived.size());
    }

    @Test
    public void checkListOfObjectsFromFile(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        List<ABaseNode> listOfNodes = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++)
                listOfNodes.add(new ClientNode(InetAddress.getByName(String.format("127.0.0.%d", i)), 20+i,String.format("node%d", i)));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        jsonParser.toFile(listOfNodes);

        Type type = new TypeToken<ArrayList<ClientNode>>() {}.getType();

        List<ABaseNode> listOfNodesReceived = (List<ABaseNode>) jsonParser.fromFile(type);

        File myObj = new File("file.json");
        myObj.delete();

        assertEquals(listOfNodes.size(), listOfNodesReceived.size());
    }
}