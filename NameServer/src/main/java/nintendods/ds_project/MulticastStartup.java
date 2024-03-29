package nintendods.ds_project;

import jakarta.annotation.PostConstruct;
import nintendods.ds_project.service.MulticastListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MulticastStartup {
    @Autowired
    private MulticastListener multicastListener;

    @PostConstruct
    public void init() {
        multicastListener.run();
    }
}
