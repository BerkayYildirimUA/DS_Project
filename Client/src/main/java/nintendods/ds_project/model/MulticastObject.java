package nintendods.ds_project.model;

public class MulticastObject {
    private long messageId;
    private String address;
    private String name;

    public MulticastObject(long id, String address, String name) {
        setMessageId(id);
        setAddress(address);
        setName(name);
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long id) {
        this.messageId = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
