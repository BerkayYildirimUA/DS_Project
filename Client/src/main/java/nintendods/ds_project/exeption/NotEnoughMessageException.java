package nintendods.ds_project.exeption;

public class NotEnoughMessageException extends Exception{
    public NotEnoughMessageException(){
        super("Not enough messages from other nodes have arrived at the new node.");
    }
}
