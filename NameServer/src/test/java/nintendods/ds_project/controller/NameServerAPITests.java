package nintendods.ds_project.controller;

import com.google.gson.Gson;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.ResponseObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.net.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class NameServerAPITests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postNodeModelTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getLocalHost(), 13, "node13");
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    ResponseObject response = gson.fromJson(result.getResponse().getContentAsString(), ResponseObject.class);
                    ClientNode data = gson.fromJson(response.getData().toString(), ClientNode.class);

                    assertEquals(node.getId(), data.getId());
                    System.out.println("id is correct");

                    assertEquals(node.getAddress().getHostAddress(), data.getAddress().getHostAddress());
                    System.out.println("address is correct");

                    assertEquals(node.getPort(), data.getPort());
                    System.out.println("port is correct");

                    System.out.println("node returned from post request is correct");
                });
    }

    // Conflict because NameServerDatabase has no "getAllNodes" method
    @Test
    public void getNodeModelTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getLocalHost(), 37, String.format("node%d", 37));
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/files")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(node))
        );


        mockMvc.perform(MockMvcRequestBuilders
                .get("/files/{id}", node.getId())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(result -> {
            ResponseObject response = gson.fromJson(result.getResponse().getContentAsString(), ResponseObject.class);
            ClientNode data = gson.fromJson(response.getData().toString(), ClientNode.class);

            assertEquals(node.getId(), data.getId());
            System.out.println("id is correct");

            assertEquals(node.getAddress().getHostAddress(), data.getAddress().getHostAddress());
            System.out.println("address is correct");

            assertEquals(node.getPort(), data.getPort());
            System.out.println("port is correct");

            System.out.println("node returned from get request is correct");
        });
    }

    @Test
    public void deleteNodeModelTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getLocalHost(), 37, String.format("node%d", 37));
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/files")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(node))
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/files/{id}", node.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    ResponseObject response = gson.fromJson(result.getResponse().getContentAsString(), ResponseObject.class);
                    ClientNode data = gson.fromJson(response.getData().toString(), ClientNode.class);

                    assertEquals(node.getId(), data.getId());
                    System.out.println("id is correct");

                    assertEquals(node.getAddress().getHostAddress(), data.getAddress().getHostAddress());
                    System.out.println("address is correct");

                    assertEquals(node.getPort(), data.getPort());
                    System.out.println("port is correct");

                    System.out.println("node returned from delete request is correct");
                });
    }
}
