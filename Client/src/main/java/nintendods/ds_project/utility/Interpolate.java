package nintendods.ds_project.utility;

public class Interpolate {

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
    public static int map(double x, double inMin, double inMax, double outMin, double outMax) {

        if(x <= inMin)
            return (int) outMin;

        if(x >= inMax)
            return (int) outMax;

        if((inMax - inMin) == 0){
            return 0;
        }

        return (int) Math.round(((x - inMin) * (outMax - outMin)) / (inMax - inMin) + outMin); // if you do double -> int, then the numbers behind the comma get cut of. So 1.9 becomes 1, which means that getting outMax is nearly impossible unless x=inMax
    }
}
