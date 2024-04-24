package nintendods.ds_project.config;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.utility.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@Configuration
public class ClientNodeConfig {

    @Value("${NODE_GLOBAL_PORT}")
    private static int NODE_GLOBAL_PORT; // Example port

    @Value("${NODE_NAME_LENGTH}")
    private static int NODE_NAME_LENGTH; // Example name length

    @Bean
    public ClientNode clientNode() throws Exception {
        return new ClientNode(InetAddress.getLocalHost(), NODE_GLOBAL_PORT, Generator.randomString(NODE_NAME_LENGTH));
    }

}
