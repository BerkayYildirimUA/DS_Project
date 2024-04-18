package nintendods.ds_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Main class to start the NameServer application without JDBC autoconfiguration.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NameServer {

    public static void main(String[] args) {
        SpringApplication.run(NameServer.class, args);
    }
}