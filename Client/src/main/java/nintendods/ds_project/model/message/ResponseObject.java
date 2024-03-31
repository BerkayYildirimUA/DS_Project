package nintendods.ds_project.model.message;

public class ResponseObject<T> {
    String message = "";
    T data;

    public ResponseObject(T data) { this.data = data; }

    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
}
