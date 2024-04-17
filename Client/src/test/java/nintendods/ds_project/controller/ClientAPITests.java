package nintendods.ds_project.controller;

import com.google.gson.Gson;
import nintendods.ds_project.database.FileDB;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;

@WebMvcTest(ClientAPI.class)
@ExtendWith(MockitoExtension.class)
public class ClientAPITests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileDB fileDB; // Mock the FileDB

    private final Gson gson = new Gson(); // Gson instance for converting objects to JSON strings

    @Test
    public void getFileLocationTest() throws Exception {
        String fileName = "testFile.txt";
        when(fileDB.getFileLocation(fileName)).thenReturn(null);  // Ensure the file is not found

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("\"File not found.\""));
    }

    @Test
    public void addFileTest() throws Exception {
        String fileName = "newFile.txt";
        String nodeIP = "10.10.10.20";
        // Assuming addOrUpdateFile is a void method; setup the mock to acknowledge its call
        doNothing().when(fileDB).addOrUpdateFile(fileName, nodeIP);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/files/")
                        .param("fileName", fileName)
                        .param("nodeIP", nodeIP)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(gson.toJson("File added/updated successfully.")));
    }

    @Test
    public void deleteFileTest() throws Exception {
        String fileName = "oldFile.txt";
        // Ensure the file exists before deleting
        when(fileDB.fileExists(fileName)).thenReturn(true);
        when(fileDB.removeFile(fileName)).thenReturn(true); // Mock the return value as true indicating successful deletion

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/files/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isOk()) // TODO: This line gives an error.
                .andExpect(MockMvcResultMatchers.content().string(gson.toJson("File deleted successfully.")));
    }

    @Test
    public void deleteNonExistentFileTest() throws Exception {
        String fileName = "nonExistentFile.txt";
        // Mock that the file does not exist
        when(fileDB.fileExists(fileName)).thenReturn(false);
        when(fileDB.removeFile(fileName)).thenReturn(false); // Mock the return value as false indicating file not found

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/files/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(gson.toJson("File not found.")));
    }

}
