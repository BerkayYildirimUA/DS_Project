package nintendods.ds_project.controller;

import com.google.gson.Gson;
import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.utility.NameToHash;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.net.*;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NameServerAPITests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postNodeTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, createStringWithKnownHash(10));
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    Map<String, String> data = gson.fromJson(result.getResponse().getContentAsString(), Map.class);
                    Map.Entry<String, String> entry = data.entrySet().stream().findFirst().get();

                    assertEquals(NameToHash.convert(node.getName()), Integer.valueOf(entry.getKey()));
                    System.out.println(String.format("id is correct : %d", Integer.valueOf(entry.getKey())));

                    assertEquals(node.getAddress().getHostAddress(), entry.getValue());
                    System.out.println("address is correct: " + entry.getValue());

                    System.out.println("node returned from post request is correct");

                    // ResponseObject response = gson.fromJson(result.getResponse().getContentAsString(), ResponseObject.class);
                    // ClientNode data = gson.fromJson(response.getData().toString(), ClientNode.class);
                    // System.out.println("Wrong object");
                });
    }

    @Test
    public void postExistingNodeTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, createStringWithKnownHash(10));
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node))
                ).andExpect(MockMvcResultMatchers.status().isOk());

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node))
                ).andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(result2 -> { System.out.println("node already exists"); });
    }

    @Test
    public void getIpFromIdTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, createStringWithKnownHash(10));
        String fileName = createStringWithKnownHash(10);
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/nodes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(node)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/files/{file_name}", fileName)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
         .andExpect(result -> {
             String data = gson.fromJson(result.getResponse().getContentAsString(), String.class);

             assertEquals(node.getAddress().getHostAddress(), data);
             System.out.println("ip is correct: " + data);

             System.out.println("ip returned from get request is correct");
         });
    }

    @Test
    public void getIpFromSmallerIdTest() throws Exception {
        ClientNode node10 = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, createStringWithKnownHash(10));
        ClientNode node20 = new ClientNode(InetAddress.getByName("10.10.10.20"), 20, createStringWithKnownHash(20));
        ClientNode node30 = new ClientNode(InetAddress.getByName("10.10.10.30"), 30, createStringWithKnownHash(30));
        String fileName = createStringWithKnownHash(0);
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node10)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node20)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node30)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/files/{file_name}", fileName)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String data = gson.fromJson(result.getResponse().getContentAsString(), String.class);

                    assertEquals(node10.getAddress().getHostAddress(), data);
                    System.out.println("ip is correct: " + data);

                    System.out.println("ip returned from get request is correct");
                });
    }

    @Test
    public void deleteWhileFetchingTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, createStringWithKnownHash(10));
        String fileName = createStringWithKnownHash(10);
        int id = 10;
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/nodes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(node))
        );

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/nodes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                 .andExpect(result -> {
                    Integer data = gson.fromJson(result.getResponse().getContentAsString(), Integer.class);

                    assertEquals(id, data);
                    System.out.println("id is correct");

                    System.out.println("node deleted");
                 });

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/files/{file_name}", fileName)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void getIpFromIdAt2NodesTest() throws Exception {
        ClientNode node = new ClientNode(InetAddress.getByName("10.10.10.10"), 10, createStringWithKnownHash(10));
        String fileName = createStringWithKnownHash(10);
        Gson gson = new Gson();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(node)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Thread.sleep(378);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/files/{file_name}", fileName)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String data = gson.fromJson(result.getResponse().getContentAsString(), String.class);

                    assertEquals(node.getAddress().getHostAddress(), data);
                    System.out.println("ip is correct: " + data);

                    System.out.println("ip returned from get request is correct");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/files/{file_name}", fileName)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String data = gson.fromJson(result.getResponse().getContentAsString(), String.class);

                    assertEquals(node.getAddress().getHostAddress(), data);
                    System.out.println("ip is correct: " + data);

                    System.out.println("ip returned from get request is correct");
                });
    }

    private String createStringWithKnownHash(int wantedHash){
        Random random = new Random();
        while (true){
            String candidate = random.ints(0, 1000).limit(30).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            if (NameToHash.convert(candidate) == wantedHash){
                return candidate;
            }
        }
    }
}
