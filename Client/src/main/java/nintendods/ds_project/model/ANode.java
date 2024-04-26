package nintendods.ds_project.model;

import java.io.Serializable;

public class ANode implements Serializable{
    private String name;

    public ANode(String name){
        setName(name);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ANode{ name='" + name + '\'' +
                '}';
    }
}
