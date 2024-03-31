package nintendods.ds_project;

import nintendods.ds_project.service.MulticastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Define the Multicast Listener Startup component to auto create it by Spring boot.
 */
@Component
public class MulticastStartup {
    @Autowired
    private MulticastService multicastService;
}