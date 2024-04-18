package nintendods.ds_project.Exeptions;

public class IDTakenExeption extends Exception{
    public IDTakenExeption(){
        super("This ID IP combo is not in DB");
    }
}
