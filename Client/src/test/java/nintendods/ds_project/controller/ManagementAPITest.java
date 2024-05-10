package nintendods.ds_project.controller;

import nintendods.ds_project.model.ClientNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientManagementAPI.class)
public class ManagementAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientNode node;

    @Test
    public void changeNextNode_test() throws Exception {
        int nextNodeId = 5;

        mockMvc.perform(put("/api/Management/nextNodeID/")
                        .param("ID", String.valueOf(nextNodeId)))
                .andExpect(status().isOk());

        verify(node).setNextNodeId(nextNodeId);
    }

    @Test
    public void changePrevNode_test() throws Exception {
        int prevNodeId = 10;

        mockMvc.perform(put("/api/Management/prevNodeID/")
                        .param("ID", String.valueOf(prevNodeId)))
                .andExpect(status().isOk());

        verify(node).setPrevNodeId(prevNodeId);
    }
}
