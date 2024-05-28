package nintendods.ds_project.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileModifier {

    private static final int maxIterations = 500000;

    public static File createFile(String directory, String fileName, boolean createNewNameIfNeeded) {
        try {
            File file;

            file = new File(createDirectory(directory), fileName);

            // Creating the file
            if (!file.createNewFile()) {
                if (createNewNameIfNeeded) {
                    // Add random chars at the end and log this to the object
                    int countIterations = 0;
                    do {
                        file = new File(file.getParent(), Generator.renameText(file.getName(), 5));
                        countIterations++;
                    } while (!file.createNewFile() || countIterations >= maxIterations);
                } else {
                    return null;
                }
            }
            else{
            }
            return file;

        } catch (IOException ex) {
            return null;
        }
    }

    public static File createFile(String fileName, boolean createNewName) {
        return FileModifier.createFile("", fileName, createNewName);
    }

    public static File createFile(String directory, String fileName, byte[] fileBytes, boolean createNewNameIfNeeded) {

        try {
            File file;

            file = new File(createDirectory(directory), fileName);

            // Creating the file
            if (!file.createNewFile()) {
                if (createNewNameIfNeeded) {
                    // Add random chars at the end and log this to the object
                    int countIterations = 0;
                    do {
                        file = new File(file.getParent(), Generator.renameText(file.getName(), 5));
                        countIterations++;
                    } while (!file.createNewFile() || countIterations >= maxIterations);

                    //Write bytes to it
                    try (FileOutputStream fos = new FileOutputStream(file);){
                        fos.write(fileBytes);
                        fos.close();
                    }
                    
                } else {
                    return null;
                }
            }
            else{
                //Write bytes to it
                try (FileOutputStream fos = new FileOutputStream(file);){
                    fos.write(fileBytes);
                }
            }
            return file;

        } catch (IOException ex) {
            return null;
        }
    }

    public static File createFile(String fileName, byte[] fileBytes, boolean createNewName) {
        return FileModifier.createFile("", fileName, fileBytes, createNewName);
    }

    public static String createDirectory(String dirName){
        // Check if directory exists

        if (!dirName.equals("")) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        } else {
            return System.getProperty("user.dir");
        }
    }
}
