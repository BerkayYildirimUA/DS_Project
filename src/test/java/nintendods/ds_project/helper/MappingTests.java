package nintendods.ds_project.helper;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class MappingTests {
    @Test
    public void checkMappingUpperLimit(){
        long oldMin = 50;
        long oldMax = 100;

        long newMin = 0;
        long newMax = 10;

        long checkValue = Mapping.map(100, oldMin, oldMax, newMin, newMax);
        long actualValue = 10;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkMappingLowerLimit(){
        long oldMin = 50;
        long oldMax = 100;

        long newMin = 0;
        long newMax = 10;

        long checkValue = Mapping.map(50, oldMin, oldMax, newMin, newMax);
        long actualValue = 0;

        assertEquals(actualValue, checkValue);
    }
    @Test
    public void checkMappingOutOfBoundNumbers(){
        long oldMin = 50;
        long oldMax = 100;

        long newMin = 0;
        long newMax = 10;

        long checkValue = Mapping.map(0, oldMin, oldMax, newMin, newMax);
        long actualValue = newMin;

        assertEquals(actualValue, checkValue);

        checkValue = Mapping.map(150, oldMin, oldMax, newMin, newMax);
        actualValue = newMax;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkMappingZero(){
        long oldMin = 0;
        long oldMax = 0;

        long newMin = 0;
        long newMax = 0;

        long checkValue = Mapping.map(0, oldMin, oldMax, newMin, newMax);
        long actualValue = newMin;

        assertEquals(actualValue, checkValue);
    }
}
