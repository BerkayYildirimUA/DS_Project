package nintendods.ds_project.model.message;

// Generic class for wrapping responses along with a message
public class ResponseObject<T> {
    String message = ""; // Message to provide additional information about the response
    T data; // Generic data type to hold any type of response data

    // Constructor to initialize the response object with data
    public ResponseObject(T data) { this.data = data; }

    // Setter for the message
    public void setMessage(String message) { this.message = message; }
    // Getter for the data
    public T getData() { return data; }
}
