package nintendods.ds_project.utility;

import java.util.Random;

public class Generator {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generates a string from the alphabeth with capital letters, lower letters and numbers.
     * @param length The length of the string.
     * @return
     */
    public static String randomString(int length) {
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
    public static int randomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

     /**
      * Replaces the text with random chars at the end. Does this on the end of
      * the text or before the last dot (for not messing up the extension).
      * 
      * @param fileName the original name
      * @param randomTextLength the amount of random chart to put after
      * @return a new text
      */
    public static String renameText(String originalText, int randomTextLength) {
        int lastIndex = originalText.lastIndexOf(".");
        String resultString = originalText;
        String randomText = Generator.randomString(randomTextLength);
        // Check if "." exists in the string
        if (lastIndex != -1) {
            resultString = originalText.substring(0, lastIndex) + randomText + originalText.substring(lastIndex);
        } else {
            // If "." does not exist, add it at the end of the file
            resultString = originalText + randomText;
        }

        return resultString;
    }
}
