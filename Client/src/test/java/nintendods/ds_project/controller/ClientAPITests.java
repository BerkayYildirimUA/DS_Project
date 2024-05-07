package nintendods.ds_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ClientAPITests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getFileLocationTest() throws Exception {
        String fileName = "testFile.txt";
        String responseJson = "\"File not found.\"";
        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(responseJson));
    }

    @Test
    public void addFileTest() throws Exception {
        String fileName = "newFile.txt";
        String nodeIP = "10.10.10.20";
        String responseJson = "\"File added/updated successfully.\"";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/files/")
                        .param("fileName", fileName)
                        .param("nodeIP", nodeIP)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(responseJson));
    }

    @Test
    public void deleteNonExistentFileTest() throws Exception {
        String fileName = "nonExistentFile.txt";
        String responseJson = "\"File not found.\"";
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/files/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(responseJson));
    }
}
