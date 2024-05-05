package nintendods.ds_project.utility;


import org.springframework.beans.factory.annotation.Value;

public class NameToHash{
    /**
     * Create a hash between 0 and 32768 based on the fileName string value
     * @param fileName contains the string representation of the filename
     * @return an integer from 0 to 32768
     */

    private static int MAX_NODES;

    @Value("${MAX_NODES}")
    public void setMaxNodes(int maxNodes) {
        NameToHash.MAX_NODES = maxNodes;
    }


    public static int convert(String fileName){
        return Interpolate.map(fileName.hashCode(), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, MAX_NODES);
    }
}