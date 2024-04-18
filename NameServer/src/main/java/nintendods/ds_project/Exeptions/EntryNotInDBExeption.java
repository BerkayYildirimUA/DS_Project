package nintendods.ds_project.Exeptions;

public class EntryNotInDBExeption extends Exception{
    public EntryNotInDBExeption(){
        super("The ID has already been taken, change the name so it's hash has a different result");
    }
}
