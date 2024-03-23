package nintendods.ds_project.Api;

import com.google.gson.Gson;
import nintendods.ds_project.model.NodeModel;
import nintendods.ds_project.model.ResponseObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class API_Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postNodeModelTest() throws Exception {
        NodeModel node = new NodeModel(InetAddress.getLocalHost(), 13, Objects.hash(String.format("node%d", 13)));
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    ResponseObject response = gson.fromJson(result.getResponse().getContentAsString(), ResponseObject.class);
                    NodeModel data = gson.fromJson(response.getData().toString(), NodeModel.class);

                    assertEquals(node.getId(), data.getId());
                    System.out.println("id is correct");

                    assertEquals(node.getAddress().getHostAddress(), data.getAddress().getHostAddress());
                    System.out.println("address is correct");

                    assertEquals(node.getPort(), data.getPort());
                    System.out.println("port is correct");

                    System.out.println("node returned from post request is correct");
                });
    }

    @Test
    public void getNodeModelTest() throws Exception {
        List<NodeModel> nodes = new ArrayList<>();
        for (int i = 1; i < 37; i+=7) nodes.add(new NodeModel(InetAddress.getLocalHost(), i, Objects.hash(String.format("node%d", i))));
        NodeModel node = new NodeModel(InetAddress.getLocalHost(), 15, Objects.hash(String.format("node%d", 15)));
        Gson gson = new Gson();

        nodes.forEach(nodeModel -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders
                        .post("/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(nodeModel))
                ).andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mockMvc.perform(MockMvcRequestBuilders
                .get("/files")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(result -> {
            List response = gson.fromJson(result.getResponse().getContentAsString(), List.class);
            List<NodeModel> responseNodes = new ArrayList<>();
            for (Object responseNode: response){ responseNodes.add(gson.fromJson(responseNode.toString(), NodeModel.class)); }
            assert(responseNodes.stream().anyMatch(data -> data.getId() == node.getId()));

            System.out.println("node returned from get request is correct");
        });
    }
}
