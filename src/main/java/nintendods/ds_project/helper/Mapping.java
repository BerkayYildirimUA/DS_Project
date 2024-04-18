package nintendods.ds_project.helper;

public class Mapping {

    /**
     * Returns a mapping number from 2 min and max to another min and max. If the actual number goes below or above the
     * input min and max values, the output will be topped off on the new min and max values.
     *
     *
     *
     * @param x the value to be converted
     * @param inMin the lower bound of the value to be converted
     * @param inMax the upper bound of the value to be converted
     * @param outMin the new lower bound
     * @param outMax the new upper bound
     * @return a number between [outMin, outMax]
     */
    public static int map(int x, int inMin, int inMax, int outMin, int outMax) {

        if(x < inMin)
            return outMin;

        if(x > inMax)
            return outMax;

        if((inMax - inMin) == 0){
            return 0;
        }

        return (int) (((x - inMin) * (outMax - outMin)) / ((inMax - inMin) + outMin)*1.0);
    }
}
