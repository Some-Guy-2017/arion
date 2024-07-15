package arion;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.*;
import java.io.*;
import java.lang.reflect.Array;

/*
 * ArionUtils provides many helpful methods for running Arion,
 * such as random string generation, converting arrays to ArrayLists, and more.
 */

public class ArionUtils {

    static final int RANDOM_WORD_COUNT = 2;
    static final String NOUN_FILEPATH = "./top-1000-nouns.txt";
    static Random rand = new Random();
    Optional<ArrayList<String>> nounListOption = parseNouns(NOUN_FILEPATH);

    /*
     * generateRandomString generates a random string by concatenating "RANDOM_WORD_COUNT" nouns
     * from "nounListOption".
     *
     * Input: no input.
     * Output: if the noun list exists, then a random string; otherwise, nothing.
     */
    public Optional<String> generateRandomString() {
        if (nounListOption == null) {
            throw new NullPointerException("Cannot generate random String with null noun list.");
        }
        if (nounListOption.isEmpty()) {
            return Optional.empty();
        }
        if (nounListOption.get().size() < 1) {
            return Optional.empty();
        }
        ArrayList<String> nounList = nounListOption.get();

        StringBuilder builder = new StringBuilder();

        int lastIdx = -1;
        for (int i = 0; i < RANDOM_WORD_COUNT; i++) {
            int idx;
            do {
                idx = rand.nextInt(nounList.size());
            } while (idx == lastIdx); // generate a random index different from
                                      // the last one
            String noun = nounList.get(idx);
            noun = noun.substring(0, 1).toUpperCase() + noun.substring(1).toLowerCase(); // format noun
            builder.append(noun);
            lastIdx = idx;
        }

        return Optional.of(builder.toString());
    }

    /*
     * generateRandomFlashcard generates a flashcard with a random front and back,
     * with the default review date and interval.
     * 
     * Input: no input.
     * Output: if generateRandomString returns a String, then a random flashcard; otherwise, nothing.
     */
    public Optional<Flashcard> generateRandomFlashcard() {
        Optional<String> front = generateRandomString();
        Optional<String> back = generateRandomString();

        if (front.isEmpty() || back.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Flashcard(front.get(), back.get()));
    }

    /*
     * toArrayList converts the input array into an ArrayList.
     *
     * Input: array.
     * Output: array as ArrayList.
     */
    public static <T> ArrayList<T> toArrayList(T[] array) {
        if (array == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /*
     * cloneArray returns a clone of the given array.
     * This method is mainly useful because it automatically checks if the
     * array is null, without having to write out the check every time.
     *
     * Input: array to clone.
     * Output: cloned array.
     */
    public static <T> T[] cloneArray(T[] arr) {
        if (arr == null) {
            return null;
        }
        return arr.clone();
    }

    /*
     * reversedArray returns a clone of the input array, reversed.
     *
     * Input: array to reverse.
     * Output: reversed array.
     */
    public static <T> T[] reversedArray(T[] array) {
        if (array == null) {
            return null;
        }
        
        T[] output = (T[]) cloneArray(array);

        int len = array.length;
        for (int i = 0; i < len; i++) {
            int j = len - i - 1;
            output[i] = array[j];
        }

        return output;
    }

    /*
     * createResourceReader creates a BufferedReader to the provided resource name.
     *
     * Input: name of the resource to read.
     * Output: reader of the resource.
     */
    public static Optional<BufferedReader> createResourceReader(Class c, String name) {
        InputStream stream = c.getResourceAsStream(name);
        if (stream == null) {
            return Optional.empty();
        }
        return Optional.of(new BufferedReader(new InputStreamReader(stream)));
    }
    
    /*
     * parseNouns parses the nouns in "NOUN_FILEPATH".
     * If an exception occurs, it returns an empty Optional.
     *
     * Input: no input.
     * Output: list of nouns read from the file, or empty if the file cannot be read.
     */
    private Optional<ArrayList<String>> parseNouns(String filepath) {
        if (filepath == null) {
            throw new NullPointerException("Cannot parse nouns with null filepath.");
        }
        
        try {
            Optional<BufferedReader> readerOption = createResourceReader(getClass(), filepath);
            if (readerOption.isEmpty()) {
                return Optional.empty();
            }
            BufferedReader reader = readerOption.get();
            ArrayList<String> nouns = new ArrayList<>();

            String line;
            Pattern nonWordPattern = Pattern.compile("\\W", Pattern.UNICODE_CHARACTER_CLASS);
            while ((line = reader.readLine()) != null) {

                // validate that the line contains only word characters
                boolean hasNonWord = nonWordPattern.matcher(line).find();
                boolean isEmpty = line.isEmpty();
                if (hasNonWord || isEmpty) {
                    return Optional.empty();
                }
                nouns.add(line);
            }
            if (nouns.size() == 0) {
                return Optional.empty();
            }
            return Optional.of(nouns);
            
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
