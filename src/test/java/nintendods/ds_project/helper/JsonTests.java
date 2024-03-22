package nintendods.ds_project.helper;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.NodeModel;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
}
