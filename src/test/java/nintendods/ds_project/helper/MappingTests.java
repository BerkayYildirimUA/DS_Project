package nintendods.ds_project.helper;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class MappingTests {
    @Test
    public void checkMappingUpperLimit(){
        int oldMin = 50;
        int oldMax = 100;

        int newMin = 0;
        int newMax = 10;

        int checkValue = Mapping.map(100, oldMin, oldMax, newMin, newMax);
        int actualValue = 10;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkMappingLowerLimit(){
        int oldMin = 50;
        int oldMax = 100;

        int newMin = 0;
        int newMax = 10;

        int checkValue = Mapping.map(50, oldMin, oldMax, newMin, newMax);
        int actualValue = 0;

        assertEquals(actualValue, checkValue);
    }
    @Test
    public void checkMappingOutOfBoundNumbers(){
        int oldMin = 50;
        int oldMax = 100;

        int newMin = 0;
        int newMax = 10;

        int checkValue = Mapping.map(0, oldMin, oldMax, newMin, newMax);
        int actualValue = newMin;

        assertEquals(actualValue, checkValue);

        checkValue = Mapping.map(150, oldMin, oldMax, newMin, newMax);
        actualValue = newMax;

        assertEquals(actualValue, checkValue);
    }

    @Test
    public void checkMappingZero(){
        int oldMin = 0;
        int oldMax = 0;

        int newMin = 0;
        int newMax = 0;

        int checkValue = Mapping.map(0, oldMin, oldMax, newMin, newMax);
        int actualValue = newMin;

        assertEquals(actualValue, checkValue);
    }
}
