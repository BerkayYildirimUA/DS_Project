package nintendods.ds_project.service;

import org.springframework.beans.factory.annotation.Value;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.FileMessage;
import nintendods.ds_project.utility.Generator;

/**
 * Transfer/ receive a file to/from another node.
 */
public class FileTranseiverService {

    //TODO: with fix of berkay to assign "static" values.
    //@Value("${tcp.file.receive.port}")
    private static int port = 12346; // this is final, do not change.

    //@Value("${tcp.file.receive.buffer}")
    private static int buffer = 50;

    private static BlockingQueue<FileMessage> receiveQueue;
    private static boolean running = false;

    private Thread receiverThread;

    /**
     * Create a File tranceiver object that will automatically create a thread where it wil listen for file receives.
     * The default TCP port is 12346 and the file capacity is 50.
     */
    public FileTranseiverService() {
        this(port, buffer);
    }

    /**
     * Create a File tranceiver object that will automatically create a thread where it wil listen for file receives.
     * 
     * @param port the receiving port to listen on
     * @param buffer the amount of files that can be buffered
     */
    public FileTranseiverService(int port, int buffer) {
        //Maintain 1 static creation of this
        if(running) return;
        running = true;

        this.receiverThread = new Thread(() -> receiveFile());
        receiveQueue = new LinkedBlockingQueue<>(buffer);
        this.receiverThread.start();

        try {
            Thread.sleep(200); // Sleep for 500 milliseconds
        } catch (InterruptedException e) {
            // Handle interruption if needed
            e.printStackTrace();
        }
    }

    public boolean sendFile(AFile fileObject, String receiverAddress) {
        Socket socket = null;

        try {
            if (fileObject == null) {
                return false;
            } // check not null
            if (!fileObject.getFile().exists()) {
                return false;
            } // Check if file exist on the system

            // Compose message
            FileMessage message = new FileMessage(fileObject);

            socket = new Socket("localhost", this.port); // Set the receivers socket
            OutputStream outputStream = socket.getOutputStream(); // get the output stream from the socket.
            // create an object output stream from the output stream so we can send an
            // object through it
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            // Send out the fmessage
            objectOutputStream.writeObject(message);

            // Close all
            // objectOutputStream.close();
            // outputStream.close();
            socket.close();

        } catch (Exception ex) {

            return false;
        }

        return true; // Succeeded
    }

    public void receiveFile() {
        try {
            ServerSocket ss = new ServerSocket(this.port);
            Socket socket;
            InputStream inputStream;
            ObjectInputStream objectInputStream;
            boolean error = false;

            System.out.println("ServerSocket awaiting connections...");

            while (!error) {
                socket = ss.accept(); // blocking call, this will wait until a connection is attempted on this
                                      // port.
                System.out.println("Connection from " + socket + "!");
                // get the input stream from the connected socket
                inputStream = socket.getInputStream();
                // create a DataInputStream so we can read data from it.
                objectInputStream = new ObjectInputStream(inputStream);

                // read the list of messages from the socket
                FileMessage receiveMessage = (FileMessage) objectInputStream.readObject();
                System.out.println("Received messages from: " + socket);
                // print out the text of every message
                System.out.println("message:" + receiveMessage.getFileObject().getName());

                receiveQueue.add(receiveMessage);
            }

            System.out.println("Closing sockets.");
            ss.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public AFile saveIncommingFile(ANode node) {
        return saveIncommingFile(node, "");
    }

    /**
     * Save a file that is present in the incoming buffer. If a file is present, we
     * save it in the given directory
     * 
     * @param newPath
     * @return
     */
    public AFile saveIncommingFile(ANode node, String directoryPath) {
        if (available() && node != null) {
            FileMessage m = getFileMessage();
            String newPath = "";

            if (m != null) {
                AFile fileObject = m.getFileObject();

                // Set the new owner of the file
                fileObject.setOwner(node);
                File f;

                try {
                    if (directoryPath.equals("")) {
                        f = new File(m.getFileObject().getName());
                    } else {
                        f = new File(directoryPath, m.getFileObject().getName());

                        File directory = new File(directoryPath);
                        if (!directory.exists()) {
                            if (directory.mkdirs()) {
                                System.out.println("Directory created successfully.");
                            }
                        }
                    }

                    // Creating the file
                    if (f.createNewFile()) {
                        System.out.println("File created successfully.");
                    } else {
                        System.out.println("File already exists. Creating a new name.");
                        //Add random chars at the end and log this to the object
                        do{
                            f = new File(f.getParent(), renameFile(f.getName(), 5));
                        } while (!f.createNewFile());
                    }

                    //Copy the received file to the created file.
                    FileOutputStream fos;
                    fos = new FileOutputStream(f);
                    fos.write(m.getFileInByte());
                    fos.close();

                    //set file path and name
                    fileObject.setPath(f.getAbsolutePath());
                    fileObject.setName(f.getName());

                    return fileObject;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Replaces the filename with random chars at the end. Does this on the end of the filename or before the last dot.
     * @param fileName
     * @return
     */
    private String renameFile(String fileName, int randomTextLength){

        int lastIndex = fileName.lastIndexOf(".");
        String resultString = fileName;
        String randomText = Generator.randomString(randomTextLength);
        // Check if "." exists in the string
        if (lastIndex != -1) {
            resultString = fileName.substring(0, lastIndex) + randomText + fileName.substring(lastIndex);
        } else {
            // If "." does not exist, add it at the end of the file
            resultString = fileName + randomText;
        }

        System.out.println(fileName + " -> " + resultString);

        return resultString;
    }

    private boolean available() {
        return !receiveQueue.isEmpty();
    }

    private FileMessage getFileMessage() {
        return receiveQueue.poll();
    }
}