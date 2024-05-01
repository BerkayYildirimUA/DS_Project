package nintendods.ds_project.utility;

import java.io.File;
import java.io.IOException;

public class FileModifier {
    public static File createFile(String directory, String fileName, boolean createNewNameIfNeeded) {
        try {
            File file;
            File dir = new File(directory);

            // Check if directory exists
            if (!directory.equals("")) {
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file = new File(dir, fileName);
            } else {
                file = new File(fileName);
            }

            // Creating the file
            if (file.createNewFile()) {
                System.out.println("File created successfully.");
            } else {
                if (createNewNameIfNeeded) {
                    //System.out.println("File already exists. Creating a new name.");

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
}
