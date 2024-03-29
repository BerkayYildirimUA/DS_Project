package nintendods.ds_project;

import nintendods.ds_project.service.NodeDBService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NameServer {

    public static void main(String[] args) {
        SpringApplication.run(NameServer.class, args);
    }
}