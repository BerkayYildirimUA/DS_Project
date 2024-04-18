package nintendods.ds_project.Exeptions;

public class IDTakenExeption extends Exception{
    public IDTakenExeption(){
        super("The ID has already been taken, change the name so it's hash has a different result");
    }
}
