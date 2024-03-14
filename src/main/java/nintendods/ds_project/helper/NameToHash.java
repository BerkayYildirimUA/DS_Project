package nintendods.ds_project.helper;

public class NameToHash{
    /**
     * Create a hash between 0 and 32768 based on the fileName string value
     * @param fileName contains the string representation of the filename
     * @return an integer from 0 to 32768
     */
    //private final int minHashValue = 0;
    //private final int maxHashValue = 32768;

    public static int convert(String fileName){
        int hash = Mapping.map((double)fileName.hashCode(), -2147483647.0, 2147483647.0, 0.0, 32768.0);
        return hash;
    }
}