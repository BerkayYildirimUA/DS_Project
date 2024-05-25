package nintendods.ds_project.exeption;

public class DuplicateFileException  extends Exception{
    public DuplicateFileException(){
        super("The given file is already present! ");
    }
    public DuplicateFileException(String extra){
        super("The given file is already present! " + extra);
    }
}
