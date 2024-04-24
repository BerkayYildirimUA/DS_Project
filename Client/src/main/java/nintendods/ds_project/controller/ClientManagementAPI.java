package nintendods.ds_project.controller;

import nintendods.ds_project.model.ClientNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Management")
public class ClientManagementAPI {

    @Autowired
    private static ClientNode node;

}
