package nintendods.ds_project.Exeptions;

/**
 * Custom exception indicating that the name server has reached its maximum capacity.
 */
public class NameServerFullExeption extends Exception{
    public NameServerFullExeption(){
        super("NameServer is full");
    }
}
