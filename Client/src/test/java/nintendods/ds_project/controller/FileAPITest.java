package nintendods.ds_project.controller;

import com.google.gson.Gson;
import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.AFile;
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
public class FileAPITest {

    @Autowired
    private MockMvc mockMvc;
    private Gson gson = new Gson();

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
        AFile file = new AFile("newFile.txt", System.getProperty("user.dir") + "/assets", new ANode("node"));
        String responseJson = "\"File added/updated successfully.\"";
        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(file)))
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
