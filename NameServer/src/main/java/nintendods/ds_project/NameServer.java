package nintendods.ds_project;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NameServer {

    private static final Logger logger = LoggerFactory.getLogger(NameServer.class);


    public static void main(String[] args) {
        SpringApplication.run(NameServer.class, args);
    }

    @PostConstruct
    public void logIpAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            String hostName = ip.getHostName();
            logger.info("Server IP address: " + ip.getHostAddress());
            logger.info("Server hostname: " + hostName);
        } catch (UnknownHostException e) {
            logger.error("Unable to get host address.", e);
        }
    }
}