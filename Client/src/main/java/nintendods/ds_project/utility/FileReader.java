package nintendods.ds_project.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    // https://stackoverflow.com/a/14676464/23665709
    public static List<File> getFiles(String directoryName) {
        File directory = new File(directoryName);
        List<File> files = new ArrayList<>();

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    files.addAll(getFiles(file.getAbsolutePath()));
                }
            }
        }
        return files;
    }
}
