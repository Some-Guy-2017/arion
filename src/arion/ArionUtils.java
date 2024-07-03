package arion;

import java.util.*;
import java.io.*;

public class ArionUtils {

    static final int randomWordCount = 2;
    static Random rand = new Random();
    static String nounFilepath = "./top-1000-nouns.txt";
    static Optional<ArrayList<String>> nounListOption = parseNouns();

    public static Optional<String> generateRandomString() {
        if (nounListOption.isEmpty())
            return Optional.empty();
        ArrayList<String> nounList = nounListOption.get();

        StringBuilder builder = new StringBuilder();

        int lastIdx = -1;
        for (int i = 0; i < randomWordCount; i++) {
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

    public static Optional<Flashcard> generateRandomFlashcard() {
        Optional<String> front = generateRandomString();
        Optional<String> back = generateRandomString();

        if (front.isEmpty() || back.isEmpty())
            return Optional.empty();
        return Optional.of(new Flashcard(front.get(), back.get()));
    }

    public static <T> ArrayList<T> toArrayList(T[] array) {
        if (array == null)
            return null;
        return new ArrayList<>(Arrays.asList(array));
    }

    public static ArrayList<Flashcard> flashcardDeepClone(ArrayList<Flashcard> list) {
        if (list == null)
            return null;
        ArrayList<Flashcard> clone = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Flashcard flashcard = list.get(i);
            if (flashcard == null) {
                clone.add(null);
                continue;
            }
            clone.add(flashcard.clone());
        }

        return clone;
    }

    public static <T> T[] cloneArray(T[] arr) {
        if (arr == null)
            return null;
        return arr.clone();
    }

    public static <T> ArrayList<T> cloneArrayList(ArrayList<T> list) {
        if (list == null)
            return null;
        ArrayList<T> clone = new ArrayList<>(list.size());
        for (T item : list)
            clone.add(item);
        return clone;
    }

    public static <T> T[] reversedArray(T[] array) {
        T[] output = (T[]) cloneArray(array);

        int len = array.length;
        for (int i = 0; i < len; i++) {
            int j = len - i - 1;
            output[i] = array[j];
        }

        return output;
    }

    private static Optional<ArrayList<String>> parseNouns() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(nounFilepath));
            return Optional.of(new ArrayList<>(reader.lines().toList()));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find " + nounFilepath);
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
