package nintendods.ds_project.utility;

import java.util.Random;

public class Generator {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generates a string from the alphabeth with capital letters, lower letters and numbers.
     * @param length The length of the string.
     * @return
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
     * Generate a random integer value between specific minimum and maximum
     * @param min Is inclusive
     * @param max Is Inclusive
     * @return the random value between min and max
     */
    public static int generateRandomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
}
