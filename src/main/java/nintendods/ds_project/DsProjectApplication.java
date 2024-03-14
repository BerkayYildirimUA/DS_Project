package nintendods.ds_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Map;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DsProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(DsProjectApplication.class, args);
    }
}