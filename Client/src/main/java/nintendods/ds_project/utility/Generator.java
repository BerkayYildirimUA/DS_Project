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
         * Renames the text by appending a random string to it, preserving the file extension if any.
         * @param name The original filename.
         * @param length The length of the random string to append.
         * @return The renamed filename with the random string appended before the extension.
         */
        public static String renameText(String name, int length) {
            int dotIndex = name.lastIndexOf('.');
            String baseName = name;
            String extension = "";

            // Check if there is an extension
            if (dotIndex > 0) {
                baseName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);  // includes the dot
            }

            // Generate random string and append it to the base name
            String randomString = randomString(length);
            return baseName + randomString + extension;
        }
    }
