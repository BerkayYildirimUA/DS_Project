package nintendods.ds_project.utility;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class InterpolateTests {
    @Test
    public void checkMappingUpperLimit(){
        int oldMin = 50;
        int oldMax = 100;

        int newMin = 0;
        int newMax = 10;

        int checkValue = Interpolate.map(100, oldMin, oldMax, newMin, newMax);
        int actualValue = 10;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkMappingLowerLimit(){
        int oldMin = 50;
        int oldMax = 100;

        int newMin = 0;
        int newMax = 10;

        int checkValue = Interpolate.map(50, oldMin, oldMax, newMin, newMax);
        int actualValue = 0;

        assertEquals(actualValue, checkValue);
    }
    @Test
    public void checkMappingOutOfBoundNumbers(){
        int oldMin = 50;
        int oldMax = 100;

        int newMin = 0;
        int newMax = 10;

        int checkValue = Interpolate.map(0, oldMin, oldMax, newMin, newMax);
        int actualValue = newMin;

        assertEquals(actualValue, checkValue);

        checkValue = Interpolate.map(150, oldMin, oldMax, newMin, newMax);
        actualValue = newMax;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkMappingZero(){
        int oldMin = 0;
        int oldMax = 0;

        int newMin = 0;
        int newMax = 0;

        int checkValue = Interpolate.map(0, oldMin, oldMax, newMin, newMax);
        int actualValue = newMin;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkExactBounds(){
        int oldMin = 0;
        int oldMax = 20;

        int newMin = 5;
        int newMax = 15;

        int checkValue1 = Interpolate.map(0, oldMin, oldMax, newMin, newMax);
        int checkValue2 = Interpolate.map(20, oldMin, oldMax, newMin, newMax);
        int actualValue1 = newMin;
        int actualValue2 = newMax;

        assertEquals(actualValue1, checkValue1);
        assertEquals(actualValue2, checkValue2);
    }
}
