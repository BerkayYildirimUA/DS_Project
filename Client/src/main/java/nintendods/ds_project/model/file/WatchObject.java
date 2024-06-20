package nintendods.ds_project.model.file;

import java.io.File;

public class WatchObject {
    private File fileWithChange;
    private eEvent kindOfChange;

    public WatchObject(File file, eEvent event){
        this.fileWithChange = file;
        this.kindOfChange = event;
    }

    public File getFileWithChange(){
        return this.fileWithChange;
    }

    public eEvent getKindOfChange(){
        return this.kindOfChange;
    }
}
