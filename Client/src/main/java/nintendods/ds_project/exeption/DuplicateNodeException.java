package nintendods.ds_project.exeption;

public class DuplicateNodeException extends Exception{

    public DuplicateNodeException(){
        super("The ID has already been taken, change the name so it's hash has a different result");
    }
}