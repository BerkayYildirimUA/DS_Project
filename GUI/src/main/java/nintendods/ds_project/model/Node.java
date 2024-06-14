package nintendods.ds_project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
    private int id;
    // other fields if necessary

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

