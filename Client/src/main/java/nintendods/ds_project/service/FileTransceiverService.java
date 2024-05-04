package nintendods.ds_project.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.FileMessage;
import nintendods.ds_project.utility.FileModifier;

/**
 * Transfer/ receive a file to/from another node.
 */
public class FileTransceiverService {

    private int port = 12346;
    private static BlockingQueue<FileMessage> receiveQueue;
    private Thread receiverThread;

    /**
     * Create a File tranceiver object that will automatically create a thread where
     * it wil listen for file receives.
     * The default TCP port is 12346 and the file capacity is 50.
     */
    public FileTransceiverService() {
        this(12346, 50);
    }

    /**
     * Create a File tranceiver object that will automatically create a thread where
     * it wil listen for file receives.
     * 
     * @param port   the receiving port to listen on
     * @param buffer the amount of files that can be buffered
     */
    public FileTransceiverService(int port, int buffer) {
        // Maintain 1 static creation of this
        // if (running)
        // return;
        // running = true;
        this.port = port;

        receiveQueue = new LinkedBlockingQueue<>(buffer);
        this.receiverThread = new Thread(() -> receiveFile(port));
        this.receiverThread.start();

        // Let the thread startup
        try {
            // Delay for 500 milliseconds
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Sends a file with its file object to a given address. The port is the default
     * one eg. 12346
     * 
     * @param fileObject      the object that contains the file information. Will be
     *                        used to retrieve the file itself.
     * @param receiverAddress the address of the receiver
     * @return true if succeeded and false if not.
     */
    public boolean sendFile(AFile fileObject, String receiverAddress) {
        Socket socket = null;

        try {
            if (fileObject == null) {
                return false;
            }
            if (!fileObject.getFile().exists()) {
                return false;
            }

            FileMessage message = new FileMessage(fileObject);

            socket = new Socket(receiverAddress, this.port); // We assume that the receiver side uses the same port.
            OutputStream outputStream = socket.getOutputStream(); // get the output stream from the socket.
            // create an object output stream from the output stream so we can send an
            // object through it
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            // Write the message over
            objectOutputStream.writeObject(message);
            socket.close();

            //File is replicated towards another node
            fileObject.setReplicated(true);
        } catch (Exception ex) {
            return false;
        }

        return true; // Succeeded
    }

    /**
     * A receiving method that will loop to infinity and listens onto the given
     * port.
     * 
     * @param port
     */
    private void receiveFile(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);
            Socket socket;
            InputStream inputStream;
            ObjectInputStream objectInputStream;
            boolean error = false;

            while (!error) {
                try {
                    socket = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.

                    inputStream = socket.getInputStream();
                    objectInputStream = new ObjectInputStream(inputStream);

                    // read the list of messages from the socket and cast to FileMessage object
                    FileMessage receiveMessage = (FileMessage) objectInputStream.readObject();
                    receiveQueue.add(receiveMessage);
                } catch (Exception ex) {
                    error = true;
                }
            }
            ss.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save a file that is present in the incoming buffer.
     * 
     * @param node the issuer who saves the file
     * @return null if nothing has arrived and an object if something has arrived.
     */
    public AFile saveIncommingFile(ANode node) {
        return saveIncommingFile(node, "");
    }

    /**
     * Save a file that is present in the incoming buffer. If a file is present, we
     * save it in the given directory.
     * 
     * @param node          the issuer who saves the file
     * @param directoryPath The new directory path to save the file in
     * @return null if nothing has arrived and an object if something has arrived.
     */
    public AFile saveIncommingFile(ANode node, String directoryPath) {
        if (available() && node != null) {
            FileMessage m = getFileMessage();

            if (m != null) {
                AFile fileObject = m.getFileObject();

                // Set the new owner of the file
                fileObject.setOwner(node);

                File f = FileModifier.createFile(directoryPath, m.getFileObject().getName(), true);

                // set file path and name
                fileObject.setPath(f.getAbsolutePath());
                fileObject.setName(f.getName());

                return fileObject;
            }
        }
        return null;
    }

    private boolean available() {
        return !receiveQueue.isEmpty();
    }

    private FileMessage getFileMessage() {
        return receiveQueue.poll();
    }
}