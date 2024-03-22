package nintendods.ds_project.helper;

import com.google.gson.reflect.TypeToken;
import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.NodeModel;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
            node = new NodeModel(InetAddress.getByName("127.0.0.1"), 20);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        };

        assertEquals("{\"address\":\"127.0.0.1\",\"port\":20}", jsonParser.toJson(node));
    }

    @Test
    public void checkSimpleParse(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        String json = "{\"address\":\"127.0.0.1\",\"port\":19}";
        NodeModel node = (NodeModel) jsonParser.toObject(json, NodeModel.class);
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
            node = new NodeModel(InetAddress.getByName("127.0.0.1"), 20);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        };

        jsonParser.toFile(node);

        ABaseNode nodeFromFile = (NodeModel) jsonParser.fromFile(NodeModel.class);

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

        ABaseNode node = (NodeModel)jsonParser.fromFile(NodeModel.class);
        assertNull(node);
    }

    @Test
    public void checkListOfObjects(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        List<ABaseNode> listOfNodes = new ArrayList<>();

        try {
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.1"), 20));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.2"), 21));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.3"), 22));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.4"), 23));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String expected = "[{\"address\":\"127.0.0.1\",\"port\":20},{\"address\":\"127.0.0.2\",\"port\":21},{\"address\":\"127.0.0.3\",\"port\":22},{\"address\":\"127.0.0.4\",\"port\":23}]";

        String jsonString = jsonParser.toJson(listOfNodes);

        assertEquals(expected, jsonString);
    }


    @Test
    public void checkListOfObjectsRevert(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        List<ABaseNode> listOfNodes = new ArrayList<>();

        try {
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.1"), 20));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.2"), 21));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.3"), 22));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.4"), 23));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String expected = "[{\"address\":\"127.0.0.1\",\"port\":20},{\"address\":\"127.0.0.2\",\"port\":21},{\"address\":\"127.0.0.3\",\"port\":22},{\"address\":\"127.0.0.4\",\"port\":23}]";

        Type type = new TypeToken<ArrayList<NodeModel>>() {}.getType();

        List<ABaseNode> listOfNodesReceived = (List<ABaseNode>) jsonParser.toObject(expected, type);

        assertEquals(listOfNodes.size(), listOfNodesReceived.size());
    }

    @Test
    public void checkListOfObjectsFromFile(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        List<ABaseNode> listOfNodes = new ArrayList<>();

        try {
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.1"), 20));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.2"), 21));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.3"), 22));
            listOfNodes.add(new NodeModel(InetAddress.getByName("127.0.0.4"), 23));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        jsonParser.toFile(listOfNodes);

        Type type = new TypeToken<ArrayList<NodeModel>>() {}.getType();

        List<ABaseNode> listOfNodesReceived = (List<ABaseNode>) jsonParser.fromFile(type);

        assertEquals(listOfNodes.size(), listOfNodesReceived.size());
    }
}
