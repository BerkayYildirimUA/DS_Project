package nintendods.ds_project;

import nintendods.ds_project.service.MulticastPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@RestController
public class DsProjectApplication {

    public static void main(String[] args) {
        MulticastPublisher mp = new MulticastPublisher();

        try {
            int x = 0;
            while(true) {
                mp.multicast("Hallo Jonge " + x);
                x++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //SpringApplication.run(DsProjectApplication.class, args);
    }
    @GetMapping("/")
    public String check() { return "Project is running"; }

}
