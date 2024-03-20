package nintendods.ds_project.helper;


import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class NameToHashTests {

    @Test
    public void checkHashValue(){

        // We know the algo for the String.hashCode().
        // the value "" = 0
        // Algo is s[0]*31^(n-1) with s[0] = 0 and n = 1 so 0*38^0 = 0
        String checkString1 = "";

        int id1 = NameToHash.convert(checkString1);

        assertEquals(32768/2, id1); // should give half of the full range.
    }

    @Test
    public void checkMapFunction(){

    }
}
