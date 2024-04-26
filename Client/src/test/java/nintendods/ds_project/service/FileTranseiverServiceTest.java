package nintendods.ds_project.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import nintendods.ds_project.model.ANetworkNode;
import nintendods.ds_project.model.ANode;
import nintendods.ds_project.model.file.AFile;
import nintendods.ds_project.model.message.FileMessage;
import nintendods.ds_project.utility.Interpolate;

@SpringBootTest
public class FileTranseiverServiceTest {
        @Test
    public void checkMappingUpperLimit(){
        try {
            File testFile = new File("TestFile.txt");
            if(testFile.createNewFile()){
                FileWriter fw = new FileWriter(testFile);
                fw.append("This is a text in the file!");
                fw.close();
            }

            ANetworkNode node = new ANetworkNode(InetAddress.getLocalHost(), 21, "Robbe");
            //Setup AFile object
            AFile fileObj = new AFile(testFile.getAbsolutePath(), testFile.getName(), node);
            
            //Create FileTranseiver object and transfer file
            FileTranseiverService ftss = new FileTranseiverService();

            ftss.sendFile(fileObj, node.getAddress().getHostAddress());

            System.out.println("Sended over");

            boolean ok = false;
            String path = "";

            while(!ok){
                if(ftss.available()){
                    FileMessage m = ftss.getFileMessage();

                    if(m != null){
                        path = m.getFileObject().getPath() + "received";
                        FileOutputStream fos = new FileOutputStream(path);
                        fos.write(m.getFileInByte());
                        fos.close();
                    }
                    ok = true;
                }
            }

            //Check if file exists on the system
            assertTrue(new File(path).exists());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } //gets the .tmp suffix
        
        //Create file object
    }
}
