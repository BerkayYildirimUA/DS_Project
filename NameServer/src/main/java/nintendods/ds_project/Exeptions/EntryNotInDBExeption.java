package nintendods.ds_project.Exeptions;

public class EntryNotInDBExeption extends Exception{
    public EntryNotInDBExeption(){
        super("This ID IP combo is not in DB");
    }
}
