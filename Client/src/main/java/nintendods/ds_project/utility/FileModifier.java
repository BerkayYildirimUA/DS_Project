package nintendods.ds_project.utility;

import java.io.File;
import java.io.IOException;

public class FileModifier {
    public static File createFile(String directory, String fileName, boolean createNewNameIfNeeded) {
        try {
            File file;

            file = new File(createDirectory(directory), fileName);

            // Creating the file
            if (!file.createNewFile()) {
                if (createNewNameIfNeeded) {
                    // Add random chars at the end and log this to the object
                    do {
                        file = new File(file.getParent(), Generator.renameText(file.getName(), 5));
                    } while (!file.createNewFile());
                } else {
                    return null;
                }
            }
            return file;

        } catch (IOException ex) {
            return null;
        }
    }

    public static File createFile(String fileName, boolean createNewName) {
        return FileModifier.createFile("", fileName, createNewName);
    }

    public static String createDirectory(String path){
        // Check if directory exists

        if (!path.equals("")) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        } else {
            return System.getProperty("user.dir");
        }
    }
}
