package nintendods.ds_project.model.message;

import nintendods.ds_project.model.file.AFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

public class FileMessage implements Serializable {
    byte[] fileInBytes;
    AFile fileObject;

    public FileMessage(AFile fileObject) {
        //Create bytes
        try {
            FileInputStream temp = new FileInputStream(fileObject.getAbsolutePath());
            this.fileInBytes = new byte[(int) fileObject.getFile().length()];
            temp.read(this.fileInBytes);
            this.fileObject = fileObject;
            temp.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public FileMessage(AFile fileObject, byte[] fileInBytes) {
        this.fileInBytes = fileInBytes;
        this.fileObject = fileObject;
    }


    public byte[] getFileInByte() {
        return this.fileInBytes;
    }

    public AFile getFileObject() {
        return this.fileObject;
    }
}
