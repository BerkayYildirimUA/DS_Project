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
        //System.out.println(fileName.hashCode());
        int hash = (int)Mapping.map(fileName.hashCode(), -2147483647, 2147483647, 0, 32768);
        return hash;
    }
}