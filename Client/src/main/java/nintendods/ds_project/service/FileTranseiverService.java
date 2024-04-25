package nintendods.ds_project.service;

import org.springframework.beans.factory.annotation.Value;
import java.io.*;
import java.net.Socket;

import nintendods.ds_project.model.file.AFile;

/**
 * Transfer/ receive a file to/from another node.
 */
public class FileTranseiverService {

    @Value("${tcp.file.receive.port}")
    private int port; // this final, do not change.

    public static boolean sendFile(AFile fileObject, int receiverPort, String receiverAddress) {
        Socket socket = null;

        try
        {
            if (fileObject == null) { return false; } //check not null
            if (!fileObject.getFile().exists()) { return false; } //Check if file exist on the system
        
            socket = new Socket(receiverAddress, receiverPort); //Ask for a port on the system.
            OutputStream outputStream = socket.getOutputStream(); // get the output stream from the socket.
            // create an object output stream from the output stream so we can send an object through it
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            //Send out the file itself



            //Send out the object
            

        
            socket.close();
        }
        catch( Exception ex){

            return false;
        }

        return true; //Succeeded
    }

    public static boolean receiveFile(AFile file, int receiverPort, String receiverAddress) {

        // Needs to startup a thread with

        return true; // Succeeded
    }

}
