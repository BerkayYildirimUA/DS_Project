package nintendods.ds_project.helper;

import nintendods.ds_project.model.ABaseNode;
import nintendods.ds_project.model.NodeModel;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        System.out.println(jsonParser.toJson(node));

        //assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkSimpleParse(){
        JsonConverter jsonParser = new JsonConverter("file.json");

        String json = "{\"address\":\"127.0.0.1\",\"port\":20}";

        System.out.println(jsonParser.toObject(json, NodeModel.class).toString());

        //assertEquals(actualValue, checkValue);
    }

}
