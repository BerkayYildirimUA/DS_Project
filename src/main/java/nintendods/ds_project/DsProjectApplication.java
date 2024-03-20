package nintendods.ds_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@RestController
public class DsProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsProjectApplication.class, args);
    }
    @GetMapping("/")
    public String check() { return "Project is running"; }

}
