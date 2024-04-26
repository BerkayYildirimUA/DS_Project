package nintendods.ds_project.model.message;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import nintendods.ds_project.model.file.AFile;

public class FileMessage implements Serializable{
    byte[] fileInByte;
    AFile fileObject;

    public FileMessage(AFile fileObject){
        //Create bytes
        try {
            FileInputStream temp = new FileInputStream(fileObject.getAbsolutePath());
            this.fileInByte = new byte[(int) fileObject.getFile().length()];
            temp.read(this.fileInByte);
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

    public byte[] getFileInByte(){
        return this.fileInByte;
    }

    public AFile getFileObject(){
        return this.fileObject;
    }
}
