package arion;

import java.util.*;
import java.io.*;

/*
 * ArionUtils provides many helpful methods for running Arion,
 * such as random string generation, converting arrays to ArrayLists, and more.
 */

public class ArionUtils {

    static final int RANDOM_WORD_COUNT = 2;
    static final String NOUN_FILEPATH = "./top-1000-nouns.txt";
    static Random rand = new Random();
    static Optional<ArrayList<String>> nounListOption = parseNouns();

    /*
     * generateRandomString generates a random string by concatenating "RANDOM_WORD_COUNT" nouns
     * from "nounListOption".
     *
     * Input: no input.
     * Output: if the noun list exists, then a random string; otherwise, nothing.
     */
    public static Optional<String> generateRandomString() {
        if (nounListOption.isEmpty())
            return Optional.empty();
        ArrayList<String> nounList = nounListOption.get();

        StringBuilder builder = new StringBuilder();

        int lastIdx = -1;
        for (int i = 0; i < RANDOM_WORD_COUNT; i++) {
            int randomIdx;
            do {
                randomIdx = rand.nextInt(nounList.size());
            } while (randomIdx == lastIdx);

            String noun = nounList.get(randomIdx);
            noun = noun.substring(0, 1).toUpperCase() + noun.substring(1).toLowerCase();
            builder.append(noun);
            lastIdx = randomIdx;
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
    public static Optional<Flashcard> generateRandomFlashcard() {
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
        T[] output = (T[]) cloneArray(array);

        int len = array.length;
        for (int i = 0; i < len; i++) {
            int j = len - i - 1;
            output[i] = array[j];
        }

        return output;
    }

    /*
     * parseNouns parses the noun from "NOUN_FILEPATH".
     * If some exception occurs (e.g. the file does not exist), it returns an empty Optional.
     *
     * Input: no input.
     * Output: list of nouns read from the file, or empty if the file cannot be read.
     */
    private static Optional<ArrayList<String>> parseNouns() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(NOUN_FILEPATH));
            return Optional.of(new ArrayList<>(reader.lines().toList()));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find " + NOUN_FILEPATH);
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
